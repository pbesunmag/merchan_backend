package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.Categoria;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.repositories.ICategoriaRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional // Garantiza que las operaciones de escritura sean atómicas y manejen rollback automático
public class ProductoService {

    private final IProductoRepository iProductoRepository;
    private final ICategoriaRepository iCategoriaRepository;

    public ProductoService(IProductoRepository iProductoRepository, ICategoriaRepository iCategoriaRepository) {
        this.iProductoRepository = iProductoRepository;
        this.iCategoriaRepository = iCategoriaRepository;
    }

    // Devuelve el catálogo de productos paginado, filtrando opcionalmente por categoría y estado de disponibilidad.
    // Consulta de solo lectura: mejora el rendimiento al no realizar cambios en la base de datos
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarProductos(Boolean activos, Long idCategoria, Pageable pageable) {
        Page<Producto> productos = iProductoRepository.buscarConFiltros(activos, idCategoria, pageable);
        return productos.map(ProductoDTO::fromEntity);
    }

    // Busca un artículo por su ID para ver su detalle o lanza error si no existe.
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return ProductoDTO.fromEntity(producto);
    }

    // Valida que el nombre no esté duplicado, asigna la categoría y crea un nuevo artículo en la tienda.
    public ProductoDTO crearProducto(CrearProductoDTO crearProductoDto) {
        Optional<Producto> productoExistente = iProductoRepository.findByNombreIgnoreCase(crearProductoDto.getNombre());

        if (productoExistente.isPresent()) {
            throw new BadRequestException("Ya existe un producto con el nombre: " + crearProductoDto.getNombre()
                    + ". ID del producto asociado: " + productoExistente.get().getId() + ".");
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
        producto.setActivo(true); // Todo nuevo producto nace habilitado por defecto

        Producto productoGuardado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoGuardado);
    }

    // Modifica los datos de un producto existente (precio, descripción, personalización) y actualiza su categoría.
    public ProductoDTO actualizarProducto(Long id, CrearProductoDTO crearProductoDto) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Valida que, si cambia el nombre, este no exista ya en otro producto diferente
        if (!producto.getNombre().equalsIgnoreCase(crearProductoDto.getNombre())
                && iProductoRepository.existsByNombreIgnoreCase(crearProductoDto.getNombre())) {
            throw new BadRequestException("Ya existe un producto con el nombre: " + crearProductoDto.getNombre());
        }

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

    // Aplica borrado lógico deshabilitando el producto sin eliminar su historial de ventas.
    public ProductoDTO desactivarProducto(Long id) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setActivo(false);

        Producto productoDesactivado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoDesactivado);
    }

    // Vuelve a habilitar un producto descatalogado para que esté disponible nuevamente en el catálogo.
    public ProductoDTO reactivarProducto(Long id) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setActivo(true);

        Producto productoReactivado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoReactivado);
    }

    // Convierte el texto enviado desde el formulario ("Sí"/"Si") al valor booleano de personalización.
    private boolean parsearEsPersonalizable(String texto) {
        return texto != null && (texto.equalsIgnoreCase("Sí") || texto.equalsIgnoreCase("Si"));
    }
}