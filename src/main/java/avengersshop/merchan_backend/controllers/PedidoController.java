package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Tag(name = "📋 Pedidos", description = "Endpoints para el control de comandas y gestión de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Operation(
            summary = "Listar pedidos paginados",
            description = "Obtiene los pedidos paginados con opción de filtrado por estado y ordenación cronológica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de pedidos obtenida con éxito")
    })
    @GetMapping
    public ResponseEntity<Page<PedidoDTO>> listarPedidos(
            @Parameter(description = "Estado del pedido (CREADO, EN_PREPARACION, LISTO, ENTREGADO)")
            @RequestParam(required = false) EstadoPedido estado,
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
    public ResponseEntity<List<PedidoPantallaDTO>> obtenerPedidosPantalla() {
        return ResponseEntity.status(HttpStatus.OK).body(
                pedidoService.listarPedidosPantalla()
        );
    }
}