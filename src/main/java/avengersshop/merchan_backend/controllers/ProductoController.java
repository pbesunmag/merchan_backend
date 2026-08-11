package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/productos (Permite listar todos o filtrar por idCategoria, activos, ordenacion)
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos(
            @RequestParam(required = false) Boolean activos,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) String ordenacion,
            @RequestParam(required = false) String tipoOrdenacion
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                productoService.listarProductos(activos, idCategoria, ordenacion, tipoOrdenacion)
        );
    }

    // GET /api/productos/{id} (Consulta un producto por su ID)
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.obtenerPorId(id));
    }

    // POST /api/productos (Crea un nuevo producto)
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody CrearProductoDTO crearProductoDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(crearProductoDto));
    }

    // PUT /api/productos/{id} (Actualiza un producto)
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody CrearProductoDTO crearProductoDto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.actualizarProducto(id, crearProductoDto));
    }

    // PATCH /api/productos/{id}/desactivar (Desactiva un producto)
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoDTO> desactivarProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.desactivarProducto(id));
    }

    // PATCH /api/productos/{id}/reactivar (Reactiva un producto)
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ProductoDTO> reactivarProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.reactivarProducto(id));
    }
}