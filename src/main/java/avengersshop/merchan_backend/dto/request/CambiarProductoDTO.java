package avengersshop.merchan_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarProductoDTO {

    @Schema(description = "ID de la línea del pedido que se devuelve", example = "1")
    @NotNull(message = "La línea del pedido es obligatoria")
    private Long lineaPedidoId;

    @Schema(description = "Unidades devueltas de la línea original", example = "2")
    @Positive(message = "La cantidad entregada debe ser mayor que cero")
    private Integer cantidadEntregada;

    @Schema(description = "Lista de artículos de destino elegidos para el canje")
    @Valid
    private List<ItemCambioDestinoDTO> nuevosArticulos;

    // Campos de compatibilidad con cambio simple (1 a 1)
    @Schema(description = "ID del producto que sustituye al original (compatibilidad 1 a 1)", example = "3")
    private Long nuevoProductoId;

    @Schema(description = "Talla del nuevo producto (compatibilidad 1 a 1)", example = "M")
    private String nuevaTalla;

    @Schema(description = "Unidades entregadas (compatibilidad 1 a 1)", example = "1")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @Schema(description = "Unidades a recibir del nuevo producto (compatibilidad 1 a 1)", example = "1")
    @Positive(message = "La cantidad nueva debe ser mayor que cero")
    private Integer cantidadNueva;
}
