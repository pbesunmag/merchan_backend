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
    private String productoNombre;
    private BigDecimal precioUnitario; // Uso de BigDecimal para mantener precisión monetaria
    private Integer cantidad;
    private String textoPersonalizado;

    public static LineaPedidoDTO fromEntity(PedidoProducto pp) {
        return new LineaPedidoDTO(
                pp.getId(),
                pp.getProducto() != null ? pp.getProducto().getId() : null,
                pp.getProducto() != null ? pp.getProducto().getNombre() : null,
                pp.getPrecioUnitario(),
                pp.getCantidad(),
                pp.getTextoPersonalizado()
        );
    }
}