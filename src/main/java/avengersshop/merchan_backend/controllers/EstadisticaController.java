package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.response.ProductoMasVendidoDTO;
import avengersshop.merchan_backend.services.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Permite solicitudes desde cualquier origen (Frontend)
@Tag(name = "📊 Estadísticas", description = "Endpoints de analítica y métricas de ventas") // Categorización en Swagger UI
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    public EstadisticaController(EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
    }

    // Obtiene el producto con más unidades vendidas.
    @Operation(
            summary = "Producto más vendido",
            description = "Devuelve el producto con más unidades vendidas, excluyendo pedidos cancelados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto más vendido obtenido con éxito"),
            @ApiResponse(responseCode = "204", description = "Aún no hay ventas registradas")
    })
    @GetMapping("/producto-mas-vendido")
    public ResponseEntity<ProductoMasVendidoDTO> obtenerProductoMasVendido() {
        return estadisticaService.obtenerProductoMasVendido()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // Obtiene el ranking completo de productos vendidos.
    @Operation(
            summary = "Ranking de productos más vendidos",
            description = "Devuelve todos los productos vendidos ordenados de mayor a menor número de unidades, excluyendo pedidos cancelados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking obtenido con éxito")
    })
    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<ProductoMasVendidoDTO>> listarProductosMasVendidos() {
        return ResponseEntity.ok(estadisticaService.listarProductosMasVendidos());
    }
}
