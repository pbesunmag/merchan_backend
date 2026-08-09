package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearTerminalDTO;
import avengersshop.merchan_backend.dto.response.TerminalDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.models.Terminal;
import avengersshop.merchan_backend.repositories.ITerminalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TerminalService {

    private final ITerminalRepository iTerminalRepository;

    public TerminalService(ITerminalRepository terminalRepository) {
        this.iTerminalRepository = terminalRepository;
    }

    // Listamos todas las terminales
    @Transactional(readOnly = true)
    public List<TerminalDTO> listarTerminales() {
        return iTerminalRepository.findAll().stream()
                .map(TerminalDTO::fromEntity)
                .toList();
    }

    // Creamos nueva terminal con validación de duplicados
    public TerminalDTO crearTerminal(CrearTerminalDTO terminalDto) {
        if (iTerminalRepository.existsByNombreIgnoreCase(terminalDto.getNombre())) {
            throw new BadRequestException("Ya existe una terminal con el nombre: " + terminalDto.getNombre());
        }

        Terminal terminal = new Terminal();
        terminal.setNombre(terminalDto.getNombre());

        Terminal terminalGuardada = iTerminalRepository.save(terminal);
        return TerminalDTO.fromEntity(terminalGuardada);
    }
}