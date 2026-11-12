package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.PromocionCreateDTO;
import avengersshop.merchan_backend.dto.response.PromocionResponseDTO;
import avengersshop.merchan_backend.services.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "🎉 Promociones", description = "Endpoints para la gestión de campañas promocionales y descuentos estacionales")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @Operation(
            summary = "Obtener promoción activa actual",
            description = "Retorna la campaña promocional vigente en la fecha actual con mayor porcentaje de descuento, o 204 No Content si no hay ninguna activa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Promoción vigente encontrada"),
            @ApiResponse(responseCode = "204", description = "No existe ninguna campaña activa en este momento")
    })
    @GetMapping("/activa")
    public ResponseEntity<PromocionResponseDTO> obtenerPromocionActiva() {
        return promocionService.obtenerPromocionVigente()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Listar todas las campañas promocionales",
            description = "Devuelve el listado completo de campañas promocionales (pasadas, presentes y futuras)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de promociones obtenido con éxito")
    })
    @GetMapping
    public ResponseEntity<List<PromocionResponseDTO>> listarPromociones() {
        return ResponseEntity.ok(promocionService.listarTodas());
    }

    @Operation(
            summary = "Crear nueva campaña promocional",
            description = "Registra una nueva promoción estacional validando rango de fechas y código único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Promoción creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o rango de fechas incoherente"),
            @ApiResponse(responseCode = "409", description = "Ya existe una promoción con el mismo código")
    })
    @PostMapping
    public ResponseEntity<PromocionResponseDTO> crearPromocion(@Valid @RequestBody PromocionCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.crearPromocion(dto));
    }

    @Operation(
            summary = "Conmutar estado activo de una promoción",
            description = "Activa o desactiva manualmente una campaña promocional por su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de activación conmutado correctamente"),
            @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
    })
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleActivo(@PathVariable Long id) {
        promocionService.toggleActivo(id);
        return ResponseEntity.ok().build();
    }
}
