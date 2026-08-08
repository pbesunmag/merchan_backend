package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.services.AvengersShopService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite peticiones desde el frontend
public class ProductoController {
    private final AvengersShopService avengersShopService;

    public ProductoController(AvengersShopService avengersShopService) {
        this.avengersShopService = avengersShopService;
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
        List<ProductoDTO> productos = avengersShopService.listarProductos(activos, idCategoria, ordenacion, tipoOrdenacion);
        return ResponseEntity.status(HttpStatus.OK).body(productos);
    }

    //Creamos nuevo producto
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody CrearProductoDTO crearProductoDto) {
        ProductoDTO nuevoProducto = avengersShopService.crearProducto(crearProductoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    //Actualizamos un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @RequestBody CrearProductoDTO crearProductoDto
    ) {
        ProductoDTO productoActualizado = avengersShopService.actualizarProducto(id, crearProductoDto);
        return ResponseEntity.status(HttpStatus.OK).body(productoActualizado);
    }

    //Desactivamos producto
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoDTO> desactivarProducto(@PathVariable Long id) {
        ProductoDTO productoDesactivado = avengersShopService.desactivarProducto(id);
        return ResponseEntity.status(HttpStatus.OK).body(productoDesactivado);
    }
}
