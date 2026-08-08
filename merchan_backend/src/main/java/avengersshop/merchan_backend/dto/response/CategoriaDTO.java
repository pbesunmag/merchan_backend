package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    private Long id;
    private String nombre;
    private String descripcion;

    public static CategoriaDTO fromEntity(Categoria categoria) {
        if (categoria == null) return null;
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}