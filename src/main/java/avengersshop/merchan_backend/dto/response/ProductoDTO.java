package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String personaje;
    private BigDecimal precio; // Coincide con BigDecimal de Producto.java
    private Integer stock;
    private String estadoStock;
    private Boolean disponible;
    private List<ProductoTallaDTO> tallas;
    private String personalizable;
    private Boolean activo;
    private String nombreCategoria;
    private String imagenUrl;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public static ProductoDTO fromEntity(Producto producto) {
        List<ProductoTallaDTO> tallasDto = producto.getTallas() != null
                ? producto.getTallas().stream().map(ProductoTallaDTO::fromEntity).toList()
                : Collections.emptyList();

        // Si el producto tiene desglose por tallas, el stock total es la suma del stock de sus tallas
        int stockActual = !tallasDto.isEmpty()
                ? tallasDto.stream().mapToInt(ProductoTallaDTO::getStock).sum()
                : (producto.getStock() != null ? producto.getStock() : 0);

        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPersonaje(),
                producto.getPrecio(),
                stockActual,
                calcularEstadoStock(stockActual),
                stockActual > 0,
                tallasDto,
                // Mapeo explicito del valor booleano a una cadena legible ("Sí" / "No")
                producto.isPersonalizable() ? "Sí" : "No",
                producto.isActivo(),
                // Extrae el nombre de la categoría evitando un posible NullPointerException
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getImagenUrl(),
                producto.getFechaCreacion(),
                producto.getFechaActualizacion()
        );
    }

    // Reglas de negocio de disponibilidad de stock para el frontend
    private static String calcularEstadoStock(int stock) {
        if (stock > 3) {
            return "En Stock";
        } else if (stock >= 1) {
            return "Pocas unidades en Stock (" + stock + " disponibles)";
        } else {
            return "Sin stock";
        }
    }
}
