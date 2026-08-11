package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (Frontend)
@Tag(name = "📦 Productos", description = "Endpoints para la gestión del catálogo e inventario de productos") // Categorización en Swagger UI
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Documentación Swagger de búsqueda paginada con filtros opcionales
    @Operation(
            summary = "Listar productos paginados",
            description = "Obtiene el listado de productos con soporte para paginación, ordenación y filtrado opcional por categoría o estado activo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de productos obtenida con éxito")
    })
    @GetMapping
    public ResponseEntity<Page<ProductoDTO>> listarProductos(
            @Parameter(description = "Filtrar por estado activo (true/false)") @RequestParam(required = false) Boolean activos,
            @Parameter(description = "ID de la categoría para filtrar") @RequestParam(required = false) Long idCategoria,
            // @PageableDefault establece paginación por defecto (Página 0, 10 elementos por página, ordenados por fecha)
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productoService.listarProductos(activos, idCategoria, pageable)
        );
    }

    @Operation(summary = "Obtener producto por ID", description = "Devuelve los detalles de un producto específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.obtenerPorId(id));
    }

    @Operation(summary = "Crear un nuevo producto", description = "Registra un producto en el catálogo previa validación de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o nombre duplicado"),
            @ApiResponse(responseCode = "404", description = "Categoría asociada no encontrada")
    })
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody CrearProductoDTO crearProductoDto) {
        // @Valid ejecuta las reglas de Bean Validation en el DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(crearProductoDto));
    }

    @Operation(summary = "Actualizar producto", description = "Modifica los datos de un producto existente por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrados")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody CrearProductoDTO crearProductoDto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.actualizarProducto(id, crearProductoDto));
    }

    @Operation(summary = "Desactivar producto", description = "Realiza un borrado lógico desactivando el producto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoDTO> desactivarProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.desactivarProducto(id));
    }

    @Operation(summary = "Reactivar producto", description = "Reactiva un producto previamente desactivado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto reactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/reactivar")
    // Permite restaurar un producto inactivo cambiando su estado sin eliminar registros
    public ResponseEntity<ProductoDTO> reactivarProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.reactivarProducto(id));
    }
}