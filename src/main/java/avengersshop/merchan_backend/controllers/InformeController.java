package avengersshop.merchan_backend.controllers;

import avengersshop.merchan_backend.services.InformeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/informes")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "📄 Informes", description = "Exportación de informes")
public class InformeController {

    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    @Operation(
            summary = "Exportar ventas en CSV",
            description = "Genera al vuelo un CSV con los pedidos del periodo. "
                    + "Columnas: CodigoPedido,Fecha,Terminal,Estado,Total,DetalleProductos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo CSV generado"),
            @ApiResponse(responseCode = "400", description = "Periodo no válido")
    })
    @GetMapping(value = "/ventas/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportarVentasCsv(
            @Parameter(description = "Periodo a exportar: dia, mes o anio", example = "mes")
            @RequestParam(defaultValue = "dia") String periodo
    ) {
        byte[] contenido = informeService.generarCsvVentas(periodo).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"informe_ventas.csv\"")
                .body(contenido);
    }

    @Operation(
            summary = "Exportar informe completo de ventas y caja",
            description = "CSV (separado por ';') con tres bloques: resumen contable, histórico de comandas "
                    + "y rendimiento por artículos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo CSV generado"),
            @ApiResponse(responseCode = "400", description = "Periodo no válido")
    })
    @CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = HttpHeaders.CONTENT_DISPOSITION)
    @GetMapping(value = "/ventas/completo", produces = "text/csv")
    public ResponseEntity<byte[]> exportarVentasCompleto(
            @Parameter(description = "Periodo a exportar: dia, mes o anio", example = "dia")
            @RequestParam(defaultValue = "dia") String periodo
    ) {
        String nombrePeriodo = InformeService.nombrePeriodo(periodo); // valida antes de generar
        byte[] contenido = informeService.generarCsvCompleto(periodo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"Avengers_Ventas_Caja_" + nombrePeriodo + ".csv\"")
                .body(contenido);
    }
}
