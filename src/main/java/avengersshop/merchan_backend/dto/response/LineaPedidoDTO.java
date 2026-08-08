package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.PedidoProducto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineaPedidoDTO {

    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String textoPersonalizado;

    public static LineaPedidoDTO fromEntity(PedidoProducto linea) {
        if (linea == null) return null;

        // Calculamos el subtotal multiplicando precio unitario por cantidad
        BigDecimal subtotal = linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()));

        return new LineaPedidoDTO(
                linea.getId(),
                linea.getProducto() != null ? linea.getProducto().getId() : null,
                linea.getProducto() != null ? linea.getProducto().getNombre() : null,
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                subtotal,
                linea.getTextoPersonalizado()
        );
    }
}