package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {
    private Long id;
    private String nombre;
    private String descripcion;

    private List<ProductoDTO> productos;

    // Método de fábrica para mapear una entidad Categoria a su correspondiente DTO de respuesta
    public static CategoriaDTO fromEntity(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());

        // Verificación de nulos para evitar NullPointerException si la lista de productos no fue inicializada
        if (categoria.getProductos() != null) {
            dto.setProductos(categoria.getProductos().stream()
                    .map(ProductoDTO::fromEntity)
                    .toList());
        }

        return dto;
    }
}