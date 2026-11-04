package avengersshop.merchan_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Petición de checkout atómico: crea el pedido completo (cabecera + líneas) en una única transacción.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPedidoDTO {

    @NotNull(message = "El ID de la terminal es obligatorio")
    private Long terminalId;

    // Cada línea reutiliza el contrato de AgregarProductoPedidoDTO (productoId, cantidad, talla, textoPersonalizado)
    @NotEmpty(message = "El pedido debe contener al menos una línea")
    private List<@Valid @NotNull AgregarProductoPedidoDTO> lineas;
}
