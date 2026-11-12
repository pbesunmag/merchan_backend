package avengersshop.merchan_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidarCuponDTO {

    @NotBlank(message = "Debe introducir un código de cupón")
    private String codigo;
}
