package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearCuponDTO;
import avengersshop.merchan_backend.dto.response.CuponDescuentoDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.DuplicateResourceException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.CuponDescuento;
import avengersshop.merchan_backend.repositories.CuponDescuentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class CuponDescuentoService {

    private final CuponDescuentoRepository cuponRepository;

    public CuponDescuentoService(CuponDescuentoRepository cuponRepository) {
        this.cuponRepository = cuponRepository;
    }

    @Transactional(readOnly = true)
    public List<CuponDescuentoDTO> listarTodos() {
        return cuponRepository.findAll()
                .stream()
                .map(CuponDescuentoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CuponDescuentoDTO> obtenerCuponDestacado() {
        LocalDateTime ahora = LocalDateTime.now();
        return cuponRepository.findByActivoTrueOrderByIdAsc()
                .stream()
                .filter(cupon -> esCuponVigente(cupon, ahora))
                .findFirst()
                .map(CuponDescuentoDTO::fromEntity);
    }

    public boolean esCuponVigente(CuponDescuento cupon, LocalDateTime ahora) {
        if (cupon == null || !Boolean.TRUE.equals(cupon.getActivo())) {
            return false;
        }
        if (cupon.getFechaInicio() != null && ahora.isBefore(cupon.getFechaInicio())) {
            return false;
        }
        if (cupon.getFechaFin() != null && ahora.isAfter(cupon.getFechaFin())) {
            return false;
        }
        if (cupon.getUsosMaximos() != null) {
            int actuales = cupon.getUsosActuales() != null ? cupon.getUsosActuales() : 0;
            if (actuales >= cupon.getUsosMaximos()) {
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public CuponDescuentoDTO validarCupon(String codigo) {
        return CuponDescuentoDTO.fromEntity(obtenerEntidadCuponValido(codigo));
    }

    @Transactional(readOnly = true)
    public CuponDescuento obtenerEntidadCuponValido(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new BadRequestException("Debe introducir un código de cupón.");
        }
        String codigoNormalizado = codigo.trim().toUpperCase();
        CuponDescuento cupon = cuponRepository.findByCodigoIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("El código de cupón '" + codigoNormalizado + "' no existe."));

        if (!Boolean.TRUE.equals(cupon.getActivo())) {
            throw new BadRequestException("El cupón '" + codigoNormalizado + "' no está activo.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (cupon.getFechaInicio() != null && ahora.isBefore(cupon.getFechaInicio())) {
            throw new BadRequestException("El cupón '" + codigoNormalizado + "' aún no ha iniciado su vigencia.");
        }
        if (cupon.getFechaFin() != null && ahora.isAfter(cupon.getFechaFin())) {
            throw new BadRequestException("El cupón '" + codigoNormalizado + "' ha caducado.");
        }
        if (cupon.getUsosMaximos() != null && cupon.getUsosActuales() != null
                && cupon.getUsosActuales() >= cupon.getUsosMaximos()) {
            throw new BadRequestException("El cupón '" + codigoNormalizado + "' ha alcanzado el límite máximo de usos.");
        }

        return cupon;
    }

    public void registrarUsoCupon(CuponDescuento cupon) {
        if (cupon == null) {
            return;
        }
        int actuales = cupon.getUsosActuales() != null ? cupon.getUsosActuales() : 0;
        cupon.setUsosActuales(actuales + 1);
        cuponRepository.save(cupon);
    }

    public CuponDescuentoDTO crearCupon(CrearCuponDTO dto) {
        String codigoNormalizado = dto.getCodigo().trim().toUpperCase();
        if (cuponRepository.findByCodigoIgnoreCase(codigoNormalizado).isPresent()) {
            throw new DuplicateResourceException("Ya existe un cupón registrado con el código: " + codigoNormalizado);
        }

        if (dto.getFechaInicio() != null && dto.getFechaFin() != null
                && !dto.getFechaFin().isAfter(dto.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        CuponDescuento nuevo = CuponDescuento.builder()
                .codigo(codigoNormalizado)
                .porcentajeDescuento(dto.getPorcentajeDescuento())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .usosMaximos(dto.getUsosMaximos())
                .usosActuales(0)
                .build();

        return CuponDescuentoDTO.fromEntity(cuponRepository.save(nuevo));
    }

    public CuponDescuentoDTO toggleActivo(Long id) {
        CuponDescuento cupon = cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado con ID: " + id));
        cupon.setActivo(!Boolean.TRUE.equals(cupon.getActivo()));
        return CuponDescuentoDTO.fromEntity(cuponRepository.save(cupon));
    }
}
