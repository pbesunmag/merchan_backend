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

    // Obtiene todas las categorías registradas en la tienda y las convierte a DTO para la respuesta.
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return iCategoriaRepository.findAll().stream()
                .map(CategoriaDTO::fromEntity)
                .toList();
    }

    // Busca una categoría por su ID o lanza una excepción si no existe en la base de datos.
    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Long id) {
        Categoria categoria = iCategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return CategoriaDTO.fromEntity(categoria);
    }

    // Valida que el nombre no esté duplicado y crea una nueva categoría en el catálogo de la tienda.
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