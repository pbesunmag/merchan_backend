package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarDescuentoProductoDTO {

    @Min(value = 0, message = "El porcentaje de descuento no puede ser negativo")
    @Max(value = 100, message = "El porcentaje de descuento no puede superar el 100%")
    private Integer porcentajeDescuento = 0;

    private Boolean tieneDescuentoFijo = false;
}
