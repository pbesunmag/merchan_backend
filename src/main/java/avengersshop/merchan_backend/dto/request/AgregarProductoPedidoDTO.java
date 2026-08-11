package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarProductoPedidoDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    // Regla de negocio: La cantidad debe estar presente y ser de al menos 1 unidad
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    private Integer cantidad;

    // Texto opcional si el producto es personalizable
    private String textoPersonalizado;
}