package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearCategoriaDTO;
import avengersshop.merchan_backend.dto.response.CategoriaDTO;
import avengersshop.merchan_backend.models.Categoria;
import avengersshop.merchan_backend.repositories.ICategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final ICategoriaRepository iCategoriaRepository;

    public CategoriaService(ICategoriaRepository iCategoriaRepository) {
        this.iCategoriaRepository = iCategoriaRepository;
    }

    //Listamos todas las categorías
    public List<CategoriaDTO> listarCategorias() {
        return iCategoriaRepository.findAll().stream()
                .map(CategoriaDTO::fromEntity).toList();
    }

    //Creamos nueva categoría
    public CategoriaDTO crearCategoria(CrearCategoriaDTO categoriaDto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDto.getNombre());
        // Manejamos un posible null si no se envía descripción para evitar fallo en BD
        categoria.setDescripcion(categoriaDto.getDescripcion() != null ? categoriaDto.getDescripcion() : "");

        Categoria categoriaGuardada = iCategoriaRepository.save(categoria);
        return CategoriaDTO.fromEntity(categoriaGuardada);
    }

}
