package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.PromocionCreateDTO;
import avengersshop.merchan_backend.dto.response.PromocionResponseDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.DuplicateResourceException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.Promocion;
import avengersshop.merchan_backend.repositories.PromocionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PromocionService {

    private final PromocionRepository promocionRepository;

    public PromocionService(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<PromocionResponseDTO> obtenerPromocionVigente() {
        return obtenerEntidadPromocionVigente()
                .map(PromocionResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<Promocion> obtenerEntidadPromocionVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Promocion> vigentesEnFecha = promocionRepository.findPromocionesVigentes(ahora);

        // 1. Promoción real activa dentro de fechas vigentes
        Optional<Promocion> promoRealEnFecha = vigentesEnFecha.stream()
                .filter(p -> !esCampanaPrueba(p))
                .findFirst();

        if (promoRealEnFecha.isPresent()) {
            return promoRealEnFecha;
        }

        // 2. Promoción real activada manualmente (activo = true), con prioridad sobre la campaña de prueba
        List<Promocion> todasActivas = promocionRepository.findByActivoTrueOrderByPorcentajeDescuentoDesc();
        Optional<Promocion> promoRealActiva = todasActivas.stream()
                .filter(p -> !esCampanaPrueba(p))
                .findFirst();

        if (promoRealActiva.isPresent()) {
            return promoRealActiva;
        }

        // 3. Campaña semilla de prueba si está vigente y activa
        return vigentesEnFecha.stream()
                .filter(this::esCampanaPrueba)
                .findFirst();
    }

    private boolean esCampanaPrueba(Promocion p) {
        if (p == null) {
            return false;
        }
        if ("PROMO_PRUEBA_ACTIVA".equalsIgnoreCase(p.getCodigo())) {
            return true;
        }
        String nombre = p.getNombre() != null ? p.getNombre().toLowerCase() : "";
        return nombre.contains("operación relámpago") || nombre.contains("operacion relampago");
    }

    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> listarTodas() {
        return promocionRepository.findAll()
                .stream()
                .map(PromocionResponseDTO::fromEntity)
                .toList();
    }

    public PromocionResponseDTO crearPromocion(PromocionCreateDTO dto) {
        if (!dto.getFechaFin().isAfter(dto.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        String codigoNormalizado = dto.getCodigo().trim().toUpperCase();
        if (promocionRepository.findByCodigo(codigoNormalizado).isPresent()) {
            throw new DuplicateResourceException("Ya existe una promoción registrada con el código: " + codigoNormalizado);
        }

        Promocion promocion = Promocion.builder()
                .codigo(codigoNormalizado)
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .porcentajeDescuento(dto.getPorcentajeDescuento())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .aplicaATodo(dto.getAplicaATodo() != null ? dto.getAplicaATodo() : true)
                .build();

        return PromocionResponseDTO.fromEntity(promocionRepository.save(promocion));
    }

    public void toggleActivo(Long id) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        promocion.setActivo(!Boolean.TRUE.equals(promocion.getActivo()));
        promocionRepository.save(promocion);
    }

    public BigDecimal calcularPrecioConDescuento(BigDecimal precioOriginal, Promocion promo) {
        if (precioOriginal == null) {
            return BigDecimal.ZERO;
        }
        if (promo == null || promo.getPorcentajeDescuento() == null || promo.getPorcentajeDescuento() <= 0) {
            return precioOriginal.setScale(2, RoundingMode.HALF_UP);
        }
        int porcentaje = promo.getPorcentajeDescuento();
        return precioOriginal
                .multiply(BigDecimal.valueOf(100 - porcentaje))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularPrecioConDescuento(BigDecimal precioOriginal, PromocionResponseDTO promo) {
        if (precioOriginal == null) {
            return BigDecimal.ZERO;
        }
        if (promo == null || promo.getPorcentajeDescuento() == null || promo.getPorcentajeDescuento() <= 0) {
            return precioOriginal.setScale(2, RoundingMode.HALF_UP);
        }
        int porcentaje = promo.getPorcentajeDescuento();
        return precioOriginal
                .multiply(BigDecimal.valueOf(100 - porcentaje))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
