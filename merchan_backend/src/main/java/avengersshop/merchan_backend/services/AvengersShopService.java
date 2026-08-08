package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearCategoriaDTO;
import avengersshop.merchan_backend.dto.request.CrearTerminalDTO;
import avengersshop.merchan_backend.dto.response.CategoriaDTO;
import avengersshop.merchan_backend.dto.response.TerminalDTO;
import avengersshop.merchan_backend.models.Categoria;
import avengersshop.merchan_backend.models.Terminal;
import avengersshop.merchan_backend.repositories.ICategoriaRepository;
import avengersshop.merchan_backend.repositories.ITerminalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvengersShopService {

    private final ITerminalRepository iTerminalRepository;
    private final ICategoriaRepository iCategoriaRepository;

    public AvengersShopService(ITerminalRepository iTerminalRepository, ICategoriaRepository iCategoriaRepository) {
        this.iTerminalRepository = iTerminalRepository;
        this.iCategoriaRepository = iCategoriaRepository;
    }

    //MÉTODOS DE TERMINALES
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

    //MÉTODOS DE CATEGORÍAS
    //Listamos todas las categorías
    public List<CategoriaDTO> listarCategorias() {
        return iCategoriaRepository.findAll().stream()
                .map(CategoriaDTO::fromEntity).toList();
    }

    //Creamos nueva categoría
    public CategoriaDTO crearCategoria(CrearCategoriaDTO categoriaDto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDto.getNombre());
        categoria.setDescripcion(categoriaDto.getDescripcion());

        Categoria categoriaGuardada = iCategoriaRepository.save(categoria);
        return CategoriaDTO.fromEntity(categoriaGuardada);
    }


}
