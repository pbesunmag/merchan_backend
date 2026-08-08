package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String personaje;
    private BigDecimal precio;
    private String personalizable; // "Sí" / "No"
    private boolean activo;
    private String nombreCategoria;

    public static ProductoDTO fromEntity(Producto producto) {
        if (producto == null) return null;

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPersonaje(),
                producto.getPrecio(),
                producto.isPersonalizable() ? "Sí" : "No",
                producto.isActivo(),
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null
        );
    }
}