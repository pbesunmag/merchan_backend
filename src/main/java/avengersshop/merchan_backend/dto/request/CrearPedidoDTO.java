package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoDTO {

    @NotNull(message = "El ID de la terminal es obligatorio para iniciar el pedido")
    private Long terminalId;
}