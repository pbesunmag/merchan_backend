package avengersshop.merchan_backend.dto.response;

import avengersshop.merchan_backend.models.Terminal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminalDTO {

    private Long id;
    private String nombre;

    public static TerminalDTO fromEntity(Terminal terminal) {
        if (terminal == null) return null;
        return new TerminalDTO(
                terminal.getId(),
                terminal.getNombre()
        );
    }
}