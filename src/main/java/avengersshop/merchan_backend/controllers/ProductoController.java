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
@CrossOrigin(origins = "*") // Permite peticiones desde el frontend
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    //Listar productos con filtros
    // Ejemplo de llamadas en Postman:
    // GET /api/productos
    // GET /api/productos?activos=true
    // GET /api/productos?idCategoria=1&ordenacion=precio&tipoOrdenacion=DESC
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos(
            @RequestParam(required = false) Boolean activos,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(required = false) String ordenacion,
            @RequestParam(required = false) String tipoOrdenacion
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.listarProductos(activos, idCategoria, ordenacion, tipoOrdenacion));
    }

    //Creamos nuevo producto
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody CrearProductoDTO crearProductoDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(crearProductoDto));
    }

    //Actualizamos un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody CrearProductoDTO crearProductoDto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.actualizarProducto(id, crearProductoDto));
    }

    //Desactivamos producto
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoDTO> desactivarProducto(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.desactivarProducto(id));
    }
}
