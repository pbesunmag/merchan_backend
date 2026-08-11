package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearCategoriaDTO {

    @NotBlank(message = "El nombre de la categoría no puede estar vacío")
    private String nombre;

    // Texto opcional para información complementaria de la categoría
    private String descripcion;
}