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

    // Atributo temático específico del dominio de la tienda (Ej: "Iron Man", "Spider-Man")
    private String personaje;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un número positivo")
    // Se usa BigDecimal para evitar errores de precisión en operaciones financieras
    private BigDecimal precio;

    // Inicialización por defecto en "No" para manejar peticiones donde no se envíe la propiedad
    private String personalizable = "No";

    // Solo pide el ID de la categoría para saber a cuál pertenece el producto
    @NotNull(message = "Debe asignar el ID de una categoría existente")
    private Long categoriaId;
}