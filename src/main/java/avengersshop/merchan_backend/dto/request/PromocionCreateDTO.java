package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionCreateDTO {

    @NotBlank(message = "El código de la promoción es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @Min(value = 1, message = "El porcentaje de descuento mínimo es 1")
    @Max(value = 100, message = "El porcentaje de descuento máximo es 100")
    private Integer porcentajeDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    private Boolean aplicaATodo = true;
}
