package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.dto.response.MetricasCajaDTO;
import avengersshop.merchan_backend.services.PostventaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caja")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "💰 Caja", description = "Balance contable y métricas de beneficio")
public class CajaController {

    private final PostventaService postventaService;

    public CajaController(PostventaService postventaService) {
        this.postventaService = postventaService;
    }

    @Operation(
            summary = "Métricas de caja del periodo",
            description = "Devuelve saldo actual, ventas, reembolsos, beneficio neto y número de transacciones "
                    + "entre el inicio del periodo (dia, mes o anio) y el momento actual."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas calculadas con éxito"),
            @ApiResponse(responseCode = "400", description = "Periodo no válido")
    })
    @GetMapping("/metricas")
    public ResponseEntity<MetricasCajaDTO> obtenerMetricas(
            @Parameter(description = "Periodo a consultar: dia, mes o anio", example = "mes")
            @RequestParam(defaultValue = "dia") String periodo
    ) {
        return ResponseEntity.ok(postventaService.obtenerMetricasBeneficio(periodo));
    }
}
