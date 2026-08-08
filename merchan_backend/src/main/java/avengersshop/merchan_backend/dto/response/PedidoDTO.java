package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private Long id;
    private String codigo;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private String nombreTerminal;
    private List<LineaPedidoDTO> lineas = new ArrayList<>();
    private BigDecimal importeTotal;

    public static PedidoDTO fromEntity(Pedido pedido) {
        if (pedido == null) return null;

        // Convertimos la lista de entidades PedidoProducto a LineaPedidoDTO usando Streams
        List<LineaPedidoDTO> lineasDTO = (pedido.getLineas() != null)
                ? pedido.getLineas().stream().map(LineaPedidoDTO::fromEntity).toList()
                : new ArrayList<>();

        // Sumamos todos los subtotales de las líneas para obtener el total del pedido
        BigDecimal total = lineasDTO.stream()
                .map(LineaPedidoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getEstado(),
                pedido.getFechaCreacion(),
                pedido.getTerminal() != null ? pedido.getTerminal().getNombre() : null,
                lineasDTO,
                total
        );
    }
}