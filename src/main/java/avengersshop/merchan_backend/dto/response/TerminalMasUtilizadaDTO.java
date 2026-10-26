package avengersshop.merchan_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalMasUtilizadaDTO {
    private Long terminalId;
    private String nombreTerminal;
    private Long totalPedidosRealizados;
}