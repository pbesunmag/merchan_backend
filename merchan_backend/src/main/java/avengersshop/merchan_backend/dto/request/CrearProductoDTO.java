package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearProductoDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;

    private String personaje; // Ej: "Iron Man", "Spider-Man", "Thanos"

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un número positivo")
    private BigDecimal precio;

    // Aceptamos "Sí" / "No" desde Postman como acordamos
    private String personalizable = "No";

    @NotNull(message = "Debe asignar el ID de una categoría existente")
    private Long categoriaId;
}