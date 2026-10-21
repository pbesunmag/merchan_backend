package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AjustarStockDTO {

    @NotBlank(message = "La operación es obligatoria (INCREMENTAR o DECREMENTAR)")
    private String operacion;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    // Opcional: permite indicar la talla a ajustar (ej. "S", "M", "L", "XL") en productos tipo Camisetas
    private String talla;
}
