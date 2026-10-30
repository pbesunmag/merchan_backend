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
public class ItemCambioDestinoDTO {

    @Schema(description = "ID del producto que se añade en el cambio", example = "3")
    @NotNull(message = "El producto de destino es obligatorio")
    private Long productoId;

    @Schema(description = "Unidades de este producto a recibir", example = "1")
    @NotNull(message = "La cantidad del artículo nuevo es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @Schema(description = "Talla del producto (obligatoria si el producto tiene tallas)", example = "M")
    private String talla;
}
