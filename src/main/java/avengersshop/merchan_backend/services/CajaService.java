package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.response.MetricasCajaDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.models.MovimientoCaja;
import avengersshop.merchan_backend.models.TipoMovimientoCaja;
import avengersshop.merchan_backend.repositories.IMovimientoCajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CajaService {

    private static final BigDecimal FONDO_BASE = new BigDecimal("500.00");

    private final IMovimientoCajaRepository movimientoCajaRepository;

    public CajaService(IMovimientoCajaRepository movimientoCajaRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
    }

    // Registra un movimiento calculando el saldo resultante a partir del último apunte.
    public MovimientoCaja registrarMovimiento(TipoMovimientoCaja tipo, BigDecimal monto, String descripcion, Long pedidoId) {
        // Lectura segura: si la caja aún no tiene apuntes se parte del fondo base de 500.00 €
        BigDecimal saldoAnterior = movimientoCajaRepository.findFirstByOrderByIdDesc()
                .map(MovimientoCaja::getSaldoResultante)
                .orElse(FONDO_BASE);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTipoMovimiento(tipo);
        movimiento.setMonto(monto);
        movimiento.setSaldoResultante(saldoAnterior.add(monto));
        movimiento.setDescripcion(descripcion);
        movimiento.setPedidoId(pedidoId);
        movimiento.setFecha(LocalDateTime.now());
        return movimientoCajaRepository.save(movimiento);
    }

    public boolean existeMovimiento(Long pedidoId, TipoMovimientoCaja tipo) {
        return movimientoCajaRepository.existsByPedidoIdAndTipoMovimiento(pedidoId, tipo);
    }

    // Calcula las métricas contables entre el inicio del periodo y ahora.
    @Transactional(readOnly = true)
    public MetricasCajaDTO calcularMetricas(String periodo) {
        LocalDateTime ahora = LocalDateTime.now();
        List<MovimientoCaja> movimientos = movimientoCajaRepository
                .findByFechaBetweenOrderByFechaAsc(inicioPeriodo(periodo), ahora);

        BigDecimal ventas = sumar(movimientos, TipoMovimientoCaja.VENTA);
        BigDecimal diferencias = sumar(movimientos, TipoMovimientoCaja.DIFERENCIA_CAMBIO);
        // Las devoluciones se guardan en negativo; se muestran como importe positivo
        BigDecimal reembolsos = sumar(movimientos, TipoMovimientoCaja.DEVOLUCION).abs();

        BigDecimal saldoActual = movimientoCajaRepository.findTopByOrderByIdDesc()
                .map(MovimientoCaja::getSaldoResultante)
                .orElse(BigDecimal.ZERO);

        int transacciones = (int) movimientos.stream()
                .filter(m -> m.getTipoMovimiento() != TipoMovimientoCaja.FONDO_INICIAL)
                .count();

        return new MetricasCajaDTO(
                saldoActual,
                ventas,
                reembolsos,
                ventas.add(diferencias).subtract(reembolsos),
                transacciones
        );
    }

    // Convierte "dia", "mes" o "anio" en la fecha-hora de inicio del periodo.
    public static LocalDateTime inicioPeriodo(String periodo) {
        if (periodo == null) {
            throw new BadRequestException("El periodo es obligatorio (dia, mes o anio).");
        }
        LocalDate hoy = LocalDate.now();
        return switch (periodo.trim().toLowerCase(Locale.ROOT)) {
            case "dia", "día", "hoy" -> hoy.atStartOfDay();
            case "mes" -> hoy.withDayOfMonth(1).atStartOfDay();
            case "anio", "año" -> hoy.withDayOfYear(1).atStartOfDay();
            default -> throw new BadRequestException("Periodo no válido: '" + periodo + "'. Use dia, mes o anio.");
        };
    }

    private BigDecimal sumar(List<MovimientoCaja> movimientos, TipoMovimientoCaja tipo) {
        return movimientos.stream()
                .filter(m -> m.getTipoMovimiento() == tipo)
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
