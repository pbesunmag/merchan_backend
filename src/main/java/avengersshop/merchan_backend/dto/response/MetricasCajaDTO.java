package avengersshop.merchan_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricasCajaDTO {

    private BigDecimal saldoActualCaja;
    private BigDecimal totalIngresosVentas;
    private BigDecimal totalReembolsos;
    // ventas + diferencias de cambio - reembolsos
    private BigDecimal beneficioNeto;
    // Movimientos del periodo (excluye el fondo inicial)
    private int totalTransacciones;
}
