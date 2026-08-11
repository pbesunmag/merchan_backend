package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearCategoriaDTO;
import avengersshop.merchan_backend.dto.response.CategoriaDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
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
                .map(CategoriaDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Long id) {
        Categoria categoria = iCategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return CategoriaDTO.fromEntity(categoria);
    }

    public CategoriaDTO crearCategoria(CrearCategoriaDTO crearCategoriaDTO) {
        if (iCategoriaRepository.existsByNombreIgnoreCase(crearCategoriaDTO.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + crearCategoriaDTO.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(crearCategoriaDTO.getNombre());
        categoria.setDescripcion(crearCategoriaDTO.getDescripcion());

        Categoria guardada = iCategoriaRepository.save(categoria);
        return CategoriaDTO.fromEntity(guardada);
    }
}