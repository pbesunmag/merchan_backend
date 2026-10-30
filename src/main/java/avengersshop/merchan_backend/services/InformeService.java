package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.MovimientoCaja;
import avengersshop.merchan_backend.models.Pedido;
import avengersshop.merchan_backend.models.PedidoProducto;
import avengersshop.merchan_backend.models.TipoMovimientoCaja;
import avengersshop.merchan_backend.repositories.IMovimientoCajaRepository;
import avengersshop.merchan_backend.repositories.IPedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InformeService {

    private static final String CABECERA = "CodigoPedido,Fecha,Terminal,Estado,Total,DetalleProductos";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final BigDecimal SALDO_INICIAL_CAJA = new BigDecimal("500.00");
    private static final String EOL = "\r\n";

    private final IPedidoRepository iPedidoRepository;
    private final IMovimientoCajaRepository iMovimientoCajaRepository;

    public InformeService(IPedidoRepository iPedidoRepository, IMovimientoCajaRepository iMovimientoCajaRepository) {
        this.iPedidoRepository = iPedidoRepository;
        this.iMovimientoCajaRepository = iMovimientoCajaRepository;
    }

    // Genera en memoria el CSV de pedidos del periodo (dia, mes o anio) hasta este momento.
    @Transactional(readOnly = true)
    public String generarCsvVentas(String periodo) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Pedido> pedidos = iPedidoRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(
                CajaService.inicioPeriodo(periodo), ahora);

        StringBuilder csv = new StringBuilder(CABECERA).append("\r\n");
        for (Pedido pedido : pedidos) {
            BigDecimal total = pedido.getProductos().stream()
                    .filter(pp -> pp.getPrecioUnitario() != null && pp.getCantidad() != null)
                    .map(pp -> pp.getPrecioUnitario().multiply(BigDecimal.valueOf(pp.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String detalle = pedido.getProductos().stream()
                    .map(this::describirLinea)
                    .collect(Collectors.joining(" | "));

            csv.append(escapar(pedido.getCodigo())).append(',')
                    .append(escapar(pedido.getFechaCreacion() != null ? pedido.getFechaCreacion().format(FORMATO_FECHA) : ""))
                    .append(',')
                    .append(escapar(pedido.getTerminal() != null ? pedido.getTerminal().getNombre() : ""))
                    .append(',')
                    .append(escapar(pedido.getEstado() != null ? pedido.getEstado().name() : ""))
                    .append(',')
                    .append(total.toPlainString()).append(',')
                    .append(escapar(detalle))
                    .append("\r\n");
        }
        return csv.toString();
    }

    // Nombre del periodo en mayúsculas para el nombre del fichero (DIA, MES, ANIO). Valida el periodo.
    public static String nombrePeriodo(String periodo) {
        CajaService.inicioPeriodo(periodo); // lanza BadRequestException si no es válido
        String p = periodo.trim().toLowerCase(Locale.ROOT);
        return p.equals("año") ? "ANIO" : p.toUpperCase(Locale.ROOT);
    }

    // Genera el informe completo (resumen contable, histórico de comandas y rendimiento por artículos).
    // Separador ';' y decimales con coma para que Excel en español lo abra directamente.
    @Transactional(readOnly = true)
    public byte[] generarCsvCompleto(String periodo) {
        LocalDateTime inicio = CajaService.inicioPeriodo(periodo);
        LocalDateTime ahora = LocalDateTime.now();

        List<MovimientoCaja> movimientos = iMovimientoCajaRepository.findByFechaBetweenOrderByFechaAsc(inicio, ahora);

        BigDecimal ventas = sumar(movimientos, TipoMovimientoCaja.VENTA);
        BigDecimal diferencias = sumar(movimientos, TipoMovimientoCaja.DIFERENCIA_CAMBIO);
        // Las devoluciones se guardan en negativo; se muestran como importe positivo
        BigDecimal devoluciones = sumar(movimientos, TipoMovimientoCaja.DEVOLUCION).abs();
        BigDecimal beneficio = ventas.add(diferencias).subtract(devoluciones);

        // Saldo de apertura: saldo previo al periodo o, si no hay historial, el fondo base de 500 €
        BigDecimal saldoInicial = iMovimientoCajaRepository.findTopByFechaBeforeOrderByIdDesc(inicio)
                .map(MovimientoCaja::getSaldoResultante)
                .orElse(SALDO_INICIAL_CAJA);
        BigDecimal saldoFinal = iMovimientoCajaRepository.findTopByOrderByIdDesc()
                .map(MovimientoCaja::getSaldoResultante)
                .orElse(saldoInicial);

        StringBuilder csv = new StringBuilder();

        // Bloque 1 - Resumen contable
        csv.append("RESUMEN CONTABLE").append(EOL);
        csv.append("Concepto;Importe (EUR)").append(EOL);
        fila(csv, "Saldo inicial en caja", dinero(saldoInicial));
        fila(csv, "Total ventas", dinero(ventas));
        fila(csv, "Total devoluciones", dinero(devoluciones));
        fila(csv, "Diferencias cobradas por cambios", dinero(diferencias));
        fila(csv, "Saldo final en caja", dinero(saldoFinal));
        fila(csv, "Beneficio neto", dinero(beneficio));
        csv.append(EOL);

        // Pedidos con impacto en caja durante el periodo
        Set<Long> pedidoIds = movimientos.stream()
                .map(MovimientoCaja::getPedidoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Pedido> pedidos = iPedidoRepository.findAllById(pedidoIds).stream()
                .sorted(Comparator.comparing(Pedido::getFechaCreacion,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        // Bloque 2 - Histórico de comandas
        csv.append("HISTORICO DE COMANDAS").append(EOL);
        csv.append("Codigo;Fecha/Hora;Estado;Terminal;Importe (EUR);Desglose").append(EOL);
        for (Pedido pedido : pedidos) {
            String estado = estadoInforme(pedido);
            String detalle = pedido.getProductos().stream()
                    .map(this::describirLineaCompleta)
                    .collect(Collectors.joining(" | "));
            fila(csv,
                    pedido.getCodigo(),
                    pedido.getFechaCreacion() != null ? pedido.getFechaCreacion().format(FORMATO_FECHA) : "",
                    estado,
                    pedido.getTerminal() != null ? pedido.getTerminal().getNombre() : "",
                    dinero(totalPedido(pedido)),
                    detalle);
        }
        csv.append(EOL);

        // Bloque 3 - Rendimiento por artículos (las devoluciones no cuentan como venta)
        Map<String, Rendimiento> rendimiento = new LinkedHashMap<>();
        for (Pedido pedido : pedidos) {
            if (pedido.getEstado() == EstadoPedido.DEVUELTO) {
                continue;
            }
            for (PedidoProducto linea : pedido.getProductos()) {
                if (linea.getCantidad() == null || linea.getPrecioUnitario() == null) {
                    continue;
                }
                String nombre = linea.getProducto() != null ? linea.getProducto().getNombre() : "?";
                String talla = linea.getTalla() != null ? linea.getTalla() : "";
                Rendimiento r = rendimiento.computeIfAbsent(nombre + "\u0000" + talla, k -> new Rendimiento(nombre, talla));
                r.unidades += linea.getCantidad();
                r.facturacion = r.facturacion.add(linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad())));
            }
        }
        csv.append("RENDIMIENTO POR ARTICULOS").append(EOL);
        csv.append("Producto;Talla;Unidades vendidas;Facturacion total (EUR)").append(EOL);
        rendimiento.values().stream()
                .sorted(Comparator.comparing((Rendimiento r) -> r.facturacion).reversed())
                .forEach(r -> fila(csv, r.nombre, r.talla, String.valueOf(r.unidades), dinero(r.facturacion)));

        // BOM UTF-8 para que Excel reconozca la codificación (tildes, €)
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    // ENTREGADO / DEVUELTO / CAMBIO (entregado con diferencia cobrada por un cambio de producto).
    private String estadoInforme(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.DEVUELTO) {
            return "DEVUELTO";
        }
        if (iMovimientoCajaRepository.existsByPedidoIdAndTipoMovimiento(pedido.getId(), TipoMovimientoCaja.DIFERENCIA_CAMBIO)) {
            return "CAMBIO";
        }
        return pedido.getEstado() != null ? pedido.getEstado().name() : "";
    }

    private BigDecimal totalPedido(Pedido pedido) {
        return pedido.getProductos().stream()
                .filter(pp -> pp.getPrecioUnitario() != null && pp.getCantidad() != null)
                .map(pp -> pp.getPrecioUnitario().multiply(BigDecimal.valueOf(pp.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumar(List<MovimientoCaja> movimientos, TipoMovimientoCaja tipo) {
        return movimientos.stream()
                .filter(m -> m.getTipoMovimiento() == tipo)
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Importe con 2 decimales y coma decimal (formato español).
    private String dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    // Añade una fila escapando cada celda.
    private void fila(StringBuilder csv, String... celdas) {
        csv.append(Arrays.stream(celdas).map(this::celda).collect(Collectors.joining(";"))).append(EOL);
    }

    // Escapa para CSV con ';' y neutraliza fórmulas (=, +, -, @) en textos.
    private String celda(String valor) {
        if (valor == null) {
            return "";
        }
        if (!valor.isEmpty() && "=+@".indexOf(valor.charAt(0)) >= 0) {
            valor = "'" + valor;
        }
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    // Ejemplo: "2x Camiseta Iron Man (M) [Grabado: Tony]"
    private String describirLineaCompleta(PedidoProducto linea) {
        String grabado = linea.getTextoPersonalizado() != null && !linea.getTextoPersonalizado().isBlank()
                ? " [Grabado: " + linea.getTextoPersonalizado() + "]"
                : "";
        return describirLinea(linea) + grabado;
    }

    // Acumulador de unidades y facturación por producto y talla.
    private static class Rendimiento {
        final String nombre;
        final String talla;
        int unidades;
        BigDecimal facturacion = BigDecimal.ZERO;

        Rendimiento(String nombre, String talla) {
            this.nombre = nombre;
            this.talla = talla;
        }
    }

    // Ejemplo: "2x Camiseta Iron Man (M)"
    private String describirLinea(PedidoProducto linea) {
        String nombre = linea.getProducto() != null ? linea.getProducto().getNombre() : "?";
        String talla = linea.getTalla() != null ? " (" + linea.getTalla() + ")" : "";
        return linea.getCantidad() + "x " + nombre + talla;
    }

    // Escapa comillas y separadores; neutraliza fórmulas (=, +, -, @) que Excel ejecutaría.
    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        if (!valor.isEmpty() && "=+-@".indexOf(valor.charAt(0)) >= 0) {
            valor = "'" + valor;
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
