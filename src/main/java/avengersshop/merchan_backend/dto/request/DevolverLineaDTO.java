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
public class DevolverLineaDTO {

    @Schema(description = "ID de la línea del pedido de la que se devuelven unidades", example = "1")
    @NotNull(message = "La línea del pedido es obligatoria")
    private Long lineaPedidoId;

    @Schema(description = "Unidades a devolver", example = "1")
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
}
