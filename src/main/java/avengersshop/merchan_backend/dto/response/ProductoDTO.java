package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String personaje;
    private BigDecimal precio; // Coincide con BigDecimal de Producto.java
    private String personalizable;
    private Boolean activo;
    private String nombreCategoria;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public static ProductoDTO fromEntity(Producto producto) {
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPersonaje(),
                producto.getPrecio(),
                // Mapeo explicito del valor booleano a una cadena legible ("Sí" / "No")
                producto.isPersonalizable() ? "Sí" : "No",
                producto.isActivo(),
                // Extrae el nombre de la categoría evitando un posible NullPointerException
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getFechaCreacion(),
                producto.getFechaActualizacion()
        );
    }
}