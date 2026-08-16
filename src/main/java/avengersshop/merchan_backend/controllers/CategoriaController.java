package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearCategoriaDTO;
import avengersshop.merchan_backend.dto.response.CategoriaDTO;
import avengersshop.merchan_backend.services.CategoriaService;
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
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (Frontend)
@Tag(name = "🏷️ Categorías", description = "Endpoints para la gestión de categorías del catálogo") // Categorización en Swagger UI
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Obtiene todas las categorías disponibles en el catálogo.
    // Documentación OpenAPI/Swagger: define el título y la descripción funcional del endpoint en la interfaz interactiva
    @Operation(
            summary = "Listar todas las categorías",
            description = "Devuelve el listado completo de categorías con la lista de sus productos anidados."
    )
    // Define los códigos de estado HTTP que puede retornar este endpoint para la documentación de Swagger
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida con éxito")
    })
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.listarCategorias());
    }

    // Obtiene el detalle de una categoría específica según su ID.
    @Operation(
            summary = "Obtener categoría por ID",
            description = "Devuelve el detalle de una categoría específica y sus productos asociados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada con éxito"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada con el ID especificado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> obtenerCategoriaPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoriaService.obtenerPorId(id));
    }

    // Registra una nueva categoría en el sistema previa validación de datos.
    @Operation(
            summary = "Crear nueva categoría",
            description = "Registra una categoría en la base de datos previa comprobación de nombres duplicados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o nombre de categoría duplicado")
    })
    @PostMapping
    public ResponseEntity<CategoriaDTO> crearCategoria(@Valid @RequestBody CrearCategoriaDTO crearCategoriaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crearCategoria(crearCategoriaDTO));
    }

    // Modifica la información general de una categoría existente.
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> modificarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CrearCategoriaDTO categoriaDto // 👈 Reutilizas el que ya existe
    ) {
        return ResponseEntity.ok(categoriaService.modificarCategoria(id, categoriaDto));
    }
}