package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.Categoria;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.repositories.ICategoriaRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoService {

    private final IProductoRepository iProductoRepository;
    private final ICategoriaRepository iCategoriaRepository;

    public ProductoService(IProductoRepository iProductoRepository, ICategoriaRepository iCategoriaRepository) {
        this.iProductoRepository = iProductoRepository;
        this.iCategoriaRepository = iCategoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos(Boolean activos, Long idCategoria, String ordenacion, String tipoOrdenacion) {

        // Configuración de la ordenación
        Sort sort = Sort.unsorted();
        if ("precio".equalsIgnoreCase(ordenacion)) {
            Sort.Direction direccion = "DESC".equalsIgnoreCase(tipoOrdenacion) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direccion, "precio");
        }

        // Delegamos todo el filtrado condicional a una sola llamada
        List<Producto> productos = iProductoRepository.buscarConFiltros(activos, idCategoria, sort);

        return productos.stream().map(ProductoDTO::fromEntity).toList();
    }

    public ProductoDTO crearProducto(CrearProductoDTO crearProductoDto) {
        // Validación de duplicados por nombre
        if (iProductoRepository.existsByNombreIgnoreCase(crearProductoDto.getNombre())) {
            throw new BadRequestException("Ya existe un producto con el nombre: " + crearProductoDto.getNombre());
        }

        Categoria categoria = iCategoriaRepository.findById(crearProductoDto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + crearProductoDto.getCategoriaId()));

        Producto producto = new Producto();
        producto.setNombre(crearProductoDto.getNombre());
        producto.setDescripcion(crearProductoDto.getDescripcion());
        producto.setPersonaje(crearProductoDto.getPersonaje());
        producto.setPrecio(crearProductoDto.getPrecio());

        boolean esPersonalizable = parsearEsPersonalizable(crearProductoDto.getPersonalizable());

        producto.setPersonalizable(esPersonalizable);
        producto.setCategoria(categoria);
        producto.setActivo(true);
        producto.setDisponible(true);

        Producto productoGuardado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoGuardado);
    }

    public ProductoDTO actualizarProducto(Long id, CrearProductoDTO crearProductoDto) {

        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        Categoria categoria = iCategoriaRepository.findById(crearProductoDto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + crearProductoDto.getCategoriaId()));

        producto.setNombre(crearProductoDto.getNombre());
        producto.setDescripcion(crearProductoDto.getDescripcion());
        producto.setPersonaje(crearProductoDto.getPersonaje());
        producto.setPrecio(crearProductoDto.getPrecio());

        boolean esPersonalizable = parsearEsPersonalizable(crearProductoDto.getPersonalizable());

        producto.setPersonalizable(esPersonalizable);
        producto.setCategoria(categoria);

        Producto productoActualizado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoActualizado);
    }

    public ProductoDTO desactivarProducto(Long id) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setActivo(false);

        Producto productoDesactivado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoDesactivado);
    }

    private boolean parsearEsPersonalizable(String texto) {
        return texto != null && (texto.equalsIgnoreCase("Sí") || texto.equalsIgnoreCase("Si"));
    }
}