package avengersshop.merchan_backend.dto.request;

import avengersshop.merchan_backend.models.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CambiarEstadoPedidoDTO {

    // Evita que la petición venga sin estado y genera un error 400 antes de llegar al servicio
    @NotNull(message = "El nuevo estado del pedido es obligatorio")
    private EstadoPedido estadoPedido;
}
