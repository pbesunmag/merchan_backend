package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.CuponDescuento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuponDescuentoDTO {

    private Long id;
    private String codigo;
    private Integer porcentajeDescuento;
    private Boolean activo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer usosMaximos;
    private Integer usosActuales;

    public static CuponDescuentoDTO fromEntity(CuponDescuento cupon) {
        if (cupon == null) {
            return null;
        }
        return CuponDescuentoDTO.builder()
                .id(cupon.getId())
                .codigo(cupon.getCodigo())
                .porcentajeDescuento(cupon.getPorcentajeDescuento())
                .activo(cupon.getActivo())
                .fechaInicio(cupon.getFechaInicio())
                .fechaFin(cupon.getFechaFin())
                .usosMaximos(cupon.getUsosMaximos())
                .usosActuales(cupon.getUsosActuales())
                .build();
    }
}
