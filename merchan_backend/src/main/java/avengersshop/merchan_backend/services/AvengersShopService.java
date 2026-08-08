package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearTerminalDTO;
import avengersshop.merchan_backend.dto.response.TerminalDTO;
import avengersshop.merchan_backend.models.Terminal;
import avengersshop.merchan_backend.repositories.ITerminalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvengersShopService {

    private final ITerminalRepository iTerminalRepository;

    public AvengersShopService(ITerminalRepository iTerminalRepository) {
        this.iTerminalRepository = iTerminalRepository;
    }

    //Listamos todas las terminales
    public List<TerminalDTO> listarTerminales() {
        return iTerminalRepository.findAll().stream()
                .map(TerminalDTO::fromEntity).toList();
    }

    //Creamos nueva terminal
    public TerminalDTO crearTerminal(CrearTerminalDTO terminalDto) {
        //Transformamos el DTO al modelo de terminal
        Terminal terminal = new Terminal();
        terminal.setNombre(terminalDto.getNombre());
        Terminal terminalGuardada = iTerminalRepository.save(terminal);
        //Convertimos el modelo guardado a TerminalDTO para devolverla
        return TerminalDTO.fromEntity(terminalGuardada);
    }


}
