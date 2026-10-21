package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.AjustarStockDTO;
import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoTallaDTO;
import avengersshop.merchan_backend.models.ProductoTalla;
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

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (Frontend)
@Tag(name = "📦 Productos", description = "Endpoints para la gestión del catálogo e inventario de productos") // Categorización en Swagger UI
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Obtiene la lista de productos paginada con filtros opcionales por estado activo o categoría.
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
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productoService.listarProductos(activos, idCategoria, pageable)
        );
    }

    // Obtiene los detalles completos de un producto específico según su ID.
    @Operation(summary = "Obtener producto por ID", description = "Devuelve los detalles de un producto específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.obtenerPorId(id));
    }

    // Obtiene el listado de tallas y su stock para un producto específico (ej. Camisetas).
    @Operation(summary = "Listar tallas de un producto", description = "Devuelve las tallas y disponibilidad de stock de un producto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tallas obtenidas correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}/tallas")
    public ResponseEntity<List<ProductoTallaDTO>> listarTallasPorProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.listarTallasPorProducto(id));
    }

    // POST: Añadir o asignar una nueva talla al producto
    @PostMapping("/{id}/tallas")
    public ResponseEntity<?> agregarTalla(
            @PathVariable Long id,
            @RequestBody ProductoTallaDTO dto) {

        // Llamada a tu servicio para guardar la talla vinculada al producto
        ProductoTalla nuevaTalla = productoService.agregarTallaAProducto(id, dto.getTalla(), dto.getStock());
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTalla);
    }

    // PUT: Actualizar el stock de una talla existente (ej: /api/productos/1/tallas/M)
    @PutMapping("/{id}/tallas/{talla}")
    public ResponseEntity<?> actualizarStockTalla(
            @PathVariable Long id,
            @PathVariable String talla,
            @RequestBody ProductoTallaDTO dto) {

        ProductoTalla tallaActualizada = productoService.actualizarStockTalla(id, talla, dto.getStock());
        return ResponseEntity.ok(tallaActualizada);
    }

    // Registra un nuevo producto en el catálogo.
    @Operation(summary = "Crear un nuevo producto", description = "Registra un producto en el catálogo previa validación de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o nombre duplicado"),
            @ApiResponse(responseCode = "404", description = "Categoría asociada no encontrada")
    })
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody CrearProductoDTO crearProductoDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(crearProductoDto));
    }

    // Modifica la información general de un producto existente.
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

    // Ajusta el stock de un producto incrementando o decrementando unidades (general o indicando talla en el body).
    @Operation(summary = "Ajustar stock de producto", description = "Incrementa o decrementa manualmente las unidades en inventario de un producto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente u operación inválida"),
            @ApiResponse(responseCode = "404", description = "Producto o talla no encontrados")
    })
    @RequestMapping(value = "/{id}/stock", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ProductoDTO> ajustarStock(
            @PathVariable Long id,
            @Valid @RequestBody AjustarStockDTO ajustarStockDto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.ajustarStock(id, ajustarStockDto));
    }

    // Ajusta el stock de una talla específica de un producto (ej. /api/productos/2/tallas/M/stock).
    @Operation(summary = "Ajustar stock por talla", description = "Incrementa o decrementa el stock de una talla específica de una camiseta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock de la talla actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente en la talla u operación inválida"),
            @ApiResponse(responseCode = "404", description = "Producto o talla no encontrados")
    })
    @RequestMapping(value = "/{id}/tallas/{talla}/stock", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ProductoDTO> ajustarStockPorTalla(
            @PathVariable Long id,
            @PathVariable String talla,
            @Valid @RequestBody AjustarStockDTO ajustarStockDto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.ajustarStockPorTalla(id, talla, ajustarStockDto));
    }

    // Aplica borrado lógico desactivando un producto para ocultarlo del catálogo activo.
    @Operation(summary = "Desactivar producto", description = "Realiza un borrado lógico desactivando el producto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoDTO> desactivarProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.desactivarProducto(id));
    }

    // Vuelve a habilitar un producto inactivo para que esté disponible en la tienda.
    @Operation(summary = "Reactivar producto", description = "Reactiva un producto previamente desactivado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto reactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ProductoDTO> reactivarProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.reactivarProducto(id));
    }
}
