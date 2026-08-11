package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearTerminalDTO;
import avengersshop.merchan_backend.dto.response.TerminalDTO;
import avengersshop.merchan_backend.services.TerminalService;
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
@RequestMapping("/api/terminales")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (Frontend)
@Tag(name = "🖥️ Terminales", description = "Endpoints para la gestión de puntos de venta y terminales físicas") // Categorización en Swagger UI
public class TerminalController {

    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    // Documentación Swagger de búsqueda paginada con filtros opcionales
    @Operation(
            summary = "Listar terminales",
            description = "Devuelve la lista completa de terminales de venta registradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de terminales obtenida con éxito")
    })
    @GetMapping
    public ResponseEntity<List<TerminalDTO>> listarTerminales() {
        return ResponseEntity.status(HttpStatus.OK).body(terminalService.listarTerminales());
    }

    @Operation(
            summary = "Crear nueva terminal",
            description = "Registra un nuevo punto de venta en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Terminal creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Nombre de terminal duplicado o datos inválidos")
    })
    @PostMapping
    public ResponseEntity<TerminalDTO> crearTerminal(@Valid @RequestBody CrearTerminalDTO crearTerminalDTO) {
        // @Valid: desencadena las restricciones definidas en el DTO (ej. @NotBlank)
        return ResponseEntity.status(HttpStatus.CREATED).body(terminalService.crearTerminal(crearTerminalDTO));
    }
}