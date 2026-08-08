package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPantallaDTO {

    private String codigo;
    private EstadoPedido estado;

    public static PedidoPantallaDTO fromEntity(Pedido pedido) {
        if (pedido == null) return null;
        return new PedidoPantallaDTO(
                pedido.getCodigo(),
                pedido.getEstado()
        );
    }
}