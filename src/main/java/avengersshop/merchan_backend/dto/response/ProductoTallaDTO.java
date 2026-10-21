package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.ProductoTalla;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoTallaDTO {

    private Long id;
    private String talla;
    private Integer stock;
    private String estadoStock;
    private Boolean disponible;

    public static ProductoTallaDTO fromEntity(ProductoTalla productoTalla) {
        int stockActual = productoTalla.getStock() != null ? productoTalla.getStock() : 0;

        return new ProductoTallaDTO(
                productoTalla.getId(),
                productoTalla.getTalla(),
                stockActual,
                calcularEstadoStock(stockActual),
                stockActual > 0
        );
    }

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
