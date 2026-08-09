package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearCategoriaDTO;
import avengersshop.merchan_backend.dto.response.CategoriaDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.models.Categoria;
import avengersshop.merchan_backend.repositories.ICategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaService {

    private final ICategoriaRepository iCategoriaRepository;

    public CategoriaService(ICategoriaRepository iCategoriaRepository) {
        this.iCategoriaRepository = iCategoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return iCategoriaRepository.findAll().stream()
                .map(CategoriaDTO::fromEntity).toList();
    }

    public CategoriaDTO crearCategoria(CrearCategoriaDTO categoriaDto) {
        // Validación de duplicados
        if (iCategoriaRepository.existsByNombreIgnoreCase(categoriaDto.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + categoriaDto.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDto.getNombre());
        categoria.setDescripcion(categoriaDto.getDescripcion() != null ? categoriaDto.getDescripcion() : "");

        Categoria categoriaGuardada = iCategoriaRepository.save(categoria);
        return CategoriaDTO.fromEntity(categoriaGuardada);
    }
}