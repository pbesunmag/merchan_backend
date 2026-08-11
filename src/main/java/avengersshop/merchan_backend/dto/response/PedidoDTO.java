package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private Long id;
    private String codigo;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String terminalNombre;
    private BigDecimal total;
    private List<LineaPedidoDTO> lineas;

    public static PedidoDTO fromEntity(Pedido pedido) {
        List<LineaPedidoDTO> lineasDTO = pedido.getProductos() != null
                ? pedido.getProductos().stream()
                .map(LineaPedidoDTO::fromEntity)
                .toList()
                : List.of();

        // Suma precisa del total usando BigDecimal
        BigDecimal totalCalculado = pedido.getProductos() != null
                ? pedido.getProductos().stream()
                .filter(pp -> pp.getPrecioUnitario() != null && pp.getCantidad() != null)
                .map(pp -> pp.getPrecioUnitario().multiply(BigDecimal.valueOf(pp.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;

        return new PedidoDTO(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getEstado(),
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion(),
                pedido.getTerminal() != null ? pedido.getTerminal().getNombre() : null,
                totalCalculado,
                lineasDTO
        );
    }
}