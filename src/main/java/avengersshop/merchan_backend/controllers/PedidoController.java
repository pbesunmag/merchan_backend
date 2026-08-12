package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.request.AgregarProductoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CambiarEstadoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CrearPedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.services.PedidoService;
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
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (Frontend)
@Tag(name = "📋 Pedidos", description = "Endpoints para el control de comandas y gestión de pedidos") // Categorización en Swagger UI
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Documentación OpenAPI/Swagger para la consulta paginada
    @Operation(
            summary = "Listar pedidos paginados",
            description = "Obtiene los pedidos paginados con opción de filtrado por estado y ordenación cronológica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de pedidos obtenida con éxito")
    })
    @GetMapping
    public ResponseEntity<Page<PedidoDTO>> listarPedidos(
            // @Parameter enriquece la documentación Swagger sobre el enum de filtrado
            @Parameter(description = "Estado del pedido (CREADO, EN_PREPARACION, LISTO, ENTREGADO)")
            @RequestParam(required = false) EstadoPedido estado,
            // @PageableDefault establece paginación por defecto (Página 0, 10 elementos por página, ordenados por fecha)
            @PageableDefault(page = 0, size = 10, sort = "fechaCreacion") Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                pedidoService.listarPedidos(estado, pageable)
        );
    }

    @Operation(
            summary = "Obtener pedido por código",
            description = "Consulta los detalles completos de un pedido a través de su código único asignado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado con éxito"),
            @ApiResponse(responseCode = "404", description = "No existe ningún pedido con ese código")
    })
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PedidoDTO> obtenerPedidoPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.status(HttpStatus.OK).body(
                pedidoService.obtenerPorCodigo(codigo)
        );
    }

    @Operation(
            summary = "Vista simplificada para pantalla",
            description = "Devuelve el listado ligero de comandas diseñado para renderizarse en las pantallas del local."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado para pantalla obtenido con éxito")
    })
    @GetMapping("/pantalla")
    // Uso de un DTO optimizado (PedidoPantallaDTO) que solo muestra los campos mínimos requeridos para la pantalla que ve el cliente
    public ResponseEntity<List<PedidoPantallaDTO>> obtenerPedidosPantalla() {
        return ResponseEntity.status(HttpStatus.OK).body(
                pedidoService.listarPedidosPantalla()
        );
    }

    @Operation(
            summary = "Crear nuevo pedido",
            description = "Inicia una nueva comanda vacía desde una terminal específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado con éxito"),
            @ApiResponse(responseCode = "404", description = "Terminal no encontrada")
    })
    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@Valid @RequestBody CrearPedidoDTO crearPedidoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(crearPedidoDTO));
    }

    @Operation(
            summary = "Añadir producto al pedido",
            description = "Agrega o incrementa un producto con su opción de personalización en un pedido en estado CREADO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto añadido al pedido con éxito"),
            @ApiResponse(responseCode = "400", description = "El pedido no está en estado CREADO o el producto no está activo"),
            @ApiResponse(responseCode = "404", description = "Pedido o producto no encontrado")
    })
    @PostMapping("/{pedidoId}/productos")
    public ResponseEntity<PedidoDTO> agregarProductoAPedido(
            @PathVariable Long pedidoId,
            @Valid @RequestBody AgregarProductoPedidoDTO agregarProductoPedidoDTO
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.agregarProductoAPedido(pedidoId, agregarProductoPedidoDTO));
    }

    @Operation(
            summary = "Eliminar producto del pedido",
            description = "Retira un producto de la comanda mientras esté en estado CREADO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto retirado del pedido"),
            @ApiResponse(responseCode = "400", description = "El pedido no está en estado CREADO"),
            @ApiResponse(responseCode = "404", description = "Pedido o producto no encontrado")
    })
    @DeleteMapping("/{pedidoId}/productos/{productoId}")
    public ResponseEntity<PedidoDTO> eliminarProductoDePedido(
            @PathVariable Long pedidoId,
            @PathVariable Long productoId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.eliminarProductoDePedido(pedidoId, productoId));
    }

    @Operation(
            summary = "Cambiar estado del pedido",
            description = "Actualiza la fase del pedido (CREADO, EN_PREPARACION, LISTO, ENTREGADO)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del pedido actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PatchMapping("/{pedidoId}/estado")
    public ResponseEntity<PedidoDTO> cambiarEstadoPedido(
            @PathVariable Long pedidoId,
            @Valid @RequestBody CambiarEstadoPedidoDTO cambiarEstadoPedidoDTO
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(pedidoService.cambiarEstadoPedido(pedidoId, cambiarEstadoPedidoDTO));
    }
}