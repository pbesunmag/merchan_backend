package avengersshop.merchan_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoMasVendidoDTO {
    private Long productoId;
    private String nombreProducto;
    private Long totalUnidadesVendidas;
}