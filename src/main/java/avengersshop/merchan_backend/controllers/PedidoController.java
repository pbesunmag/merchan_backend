package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.AgregarProductoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CambiarEstadoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CrearPedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.services.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*") // Permite peticiones desde el frontend
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    //Listamos los pedidos filtrados por estado
    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listarPedidos(@RequestParam(required = false)EstadoPedido estadoPedido) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.listarPedidosPorEstado(estadoPedido));
    }

    // Buscamos un pedido por su código
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PedidoDTO> obtenerPedidoPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.obtenerPorCodigo(codigo));
    }

    // Listamos pedidos simplificados para la pantalla de AvengersShop con su código y estado
    @GetMapping("/pantalla")
    public ResponseEntity<List<PedidoPantallaDTO>> obtenerPedidosPantalla() {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.obtenerPedidosParaPantalla());
    }

    // Creamos un nuevo pedido desde una terminal
    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@Valid @RequestBody CrearPedidoDTO crearPedidoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(crearPedidoDTO));
    }

    // Añadimos un producto al pedido
    @PostMapping("/{pedidoId}/productos")
    public ResponseEntity<PedidoDTO> agregarProductoAPedido(@PathVariable Long pedidoId, @Valid @RequestBody AgregarProductoPedidoDTO agregarProductoPedidoDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.agregarProductoAPedido(pedidoId, agregarProductoPedidoDTO));
    }

    // Eliminamos un producto del pedido
    @DeleteMapping("/{pedidoId}/productos/{productoId}")
    public ResponseEntity<PedidoDTO> eliminarProductoDePedido(@PathVariable Long pedidoId, @PathVariable Long productoId) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.eliminarProductoDePedido(pedidoId, productoId));
    }

    // Cambiamos el estado de un pedido
    @PatchMapping("/{pedidoId}/estado")
    public ResponseEntity<PedidoDTO> cambiarEstadoPedido(@PathVariable Long pedidoId, @Valid @RequestBody CambiarEstadoPedidoDTO cambiarEstadoPedidoDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.cambiarEstadoPedido(pedidoId, cambiarEstadoPedidoDTO));
    }
}
