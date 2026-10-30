package avengersshop.merchan_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarProductoDTO {

    @Schema(description = "ID de la línea del pedido que se devuelve", example = "1")
    @NotNull(message = "La línea del pedido es obligatoria")
    private Long lineaPedidoId;

    @Schema(description = "ID del producto que sustituye al original", example = "5")
    @NotNull(message = "El nuevo producto es obligatorio")
    private Long nuevoProductoId;

    @Schema(description = "Talla del nuevo producto (obligatoria si tiene tallas)", example = "M")
    private String nuevaTalla;

    @Schema(description = "Unidades a cambiar", example = "1")
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
}
