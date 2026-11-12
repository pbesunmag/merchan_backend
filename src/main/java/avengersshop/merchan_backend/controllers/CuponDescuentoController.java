package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearCuponDTO;
import avengersshop.merchan_backend.dto.request.ValidarCuponDTO;
import avengersshop.merchan_backend.dto.response.CuponDescuentoDTO;
import avengersshop.merchan_backend.services.CuponDescuentoService;
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
@RequestMapping("/api/cupones")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "🎟️ Cupones", description = "Endpoints para la validación y administración de cupones promocionales")
public class CuponDescuentoController {

    private final CuponDescuentoService cuponService;

    public CuponDescuentoController(CuponDescuentoService cuponService) {
        this.cuponService = cuponService;
    }

    @Operation(
            summary = "Obtener cupón de descuento destacado",
            description = "Devuelve el primer cupón vigente para anunciar en la tienda o 204 No Content si no hay ninguno disponible."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupón destacado encontrado"),
            @ApiResponse(responseCode = "204", description = "No hay ningún cupón vigente disponible")
    })
    @GetMapping("/destacado")
    public ResponseEntity<CuponDescuentoDTO> obtenerCuponDestacado() {
        return cuponService.obtenerCuponDestacado()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Validar cupón de descuento",
            description = "Comprueba si un código de cupón existe, está activo, dentro de fechas y con usos disponibles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupón válido"),
            @ApiResponse(responseCode = "400", description = "Cupón inactivo, caducado o sin usos disponibles"),
            @ApiResponse(responseCode = "404", description = "Cupón no encontrado")
    })
    @PostMapping("/validar")
    public ResponseEntity<CuponDescuentoDTO> validarCupon(@Valid @RequestBody ValidarCuponDTO dto) {
        return ResponseEntity.ok(cuponService.validarCupon(dto.getCodigo()));
    }

    @Operation(
            summary = "Listar todos los cupones",
            description = "Devuelve el listado completo de cupones de descuento registrados."
    )
    @GetMapping
    public ResponseEntity<List<CuponDescuentoDTO>> listarCupones() {
        return ResponseEntity.ok(cuponService.listarTodos());
    }

    @Operation(
            summary = "Crear nuevo cupón de descuento",
            description = "Registra un nuevo código promocional en el sistema."
    )
    @PostMapping
    public ResponseEntity<CuponDescuentoDTO> crearCupon(@Valid @RequestBody CrearCuponDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuponService.crearCupon(dto));
    }

    @Operation(
            summary = "Conmutar estado activo de un cupón",
            description = "Activa o desactiva manualmente un cupón de descuento."
    )
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CuponDescuentoDTO> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(cuponService.toggleActivo(id));
    }
}
