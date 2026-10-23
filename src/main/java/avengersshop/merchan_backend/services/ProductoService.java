package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.AjustarStockDTO;
import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.dto.response.ProductoTallaDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.Categoria;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.models.ProductoTalla;
import avengersshop.merchan_backend.repositories.ICategoriaRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import avengersshop.merchan_backend.repositories.IProductoTallaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional // Garantiza que las operaciones de escritura sean atómicas y manejen rollback automático
public class ProductoService {

    private static final List<String> TALLAS_POR_DEFECTO = List.of("S", "M", "L", "XL");
    private static final String TOPIC_PRODUCTOS_STOCK = "/topic/productos-stock";

    private final IProductoRepository iProductoRepository;
    private final ICategoriaRepository iCategoriaRepository;
    private final IProductoTallaRepository iProductoTallaRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ProductoService(IProductoRepository iProductoRepository,
                           ICategoriaRepository iCategoriaRepository,
                           IProductoTallaRepository iProductoTallaRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.iProductoRepository = iProductoRepository;
        this.iCategoriaRepository = iCategoriaRepository;
        this.iProductoTallaRepository = iProductoTallaRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private void notificarActualizacionStock(ProductoDTO productoActualizado) {
        if (messagingTemplate != null && productoActualizado != null) {
            messagingTemplate.convertAndSend(TOPIC_PRODUCTOS_STOCK, productoActualizado);
        }
    }

    // Devuelve el catálogo de productos paginado, filtrando opcionalmente por categoría y estado de disponibilidad.
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

    // Obtiene las tallas asociadas a un producto específico.
    @Transactional(readOnly = true)
    public List<ProductoTallaDTO> listarTallasPorProducto(Long productoId) {
        if (!iProductoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }
        return iProductoTallaRepository.findByProductoId(productoId).stream()
                .map(ProductoTallaDTO::fromEntity)
                .toList();
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
        producto.setStock(crearProductoDto.getStock() != null ? crearProductoDto.getStock() : 10);

        boolean esPersonalizable = parsearEsPersonalizable(crearProductoDto.getPersonalizable());

        producto.setPersonalizable(esPersonalizable);
        producto.setCategoria(categoria);
        producto.setActivo(true); // Todo nuevo producto nace habilitado por defecto

        // Si pertenece a la categoría Camisetas, inicializa sus tallas por defecto (S, M, L, XL)
        if (esCategoriaCamisetas(categoria)) {
            for (String tallaNombre : TALLAS_POR_DEFECTO) {
                ProductoTalla pt = new ProductoTalla();
                pt.setProducto(producto);
                pt.setTalla(tallaNombre);
                pt.setStock(5);
                producto.getTallas().add(pt);
            }
            producto.setStock(producto.getTallas().stream().mapToInt(ProductoTalla::getStock).sum());
        }

        Producto productoGuardado = iProductoRepository.save(producto);
        ProductoDTO dto = ProductoDTO.fromEntity(productoGuardado);
        notificarActualizacionStock(dto);
        return dto;
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
        if (crearProductoDto.getStock() != null && (producto.getTallas() == null || producto.getTallas().isEmpty())) {
            producto.setStock(crearProductoDto.getStock());
        }

        boolean esPersonalizable = parsearEsPersonalizable(crearProductoDto.getPersonalizable());

        producto.setPersonalizable(esPersonalizable);
        producto.setCategoria(categoria);

        Producto productoActualizado = iProductoRepository.save(producto);
        ProductoDTO dto = ProductoDTO.fromEntity(productoActualizado);
        notificarActualizacionStock(dto);
        return dto;
    }

    // Ajusta el stock manualmente (general o de una talla específica si se envía 'talla').
    public ProductoDTO ajustarStock(Long id, AjustarStockDTO ajustarStockDto) {
        if (ajustarStockDto.getTalla() != null && !ajustarStockDto.getTalla().isBlank()) {
            return ajustarStockPorTalla(id, ajustarStockDto.getTalla(), ajustarStockDto);
        }

        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        int cantidad = ajustarStockDto.getCantidad();
        String operacion = ajustarStockDto.getOperacion().trim().toUpperCase();

        if ("INCREMENTAR".equals(operacion)) {
            producto.setStock(stockActual + cantidad);
        } else if ("DECREMENTAR".equals(operacion)) {
            if (stockActual - cantidad < 0) {
                throw new BadRequestException("Stock insuficiente");
            }
            producto.setStock(stockActual - cantidad);
        } else {
            throw new BadRequestException("Operación no válida. Use INCREMENTAR o DECREMENTAR.");
        }

        Producto productoActualizado = iProductoRepository.save(producto);
        ProductoDTO dto = ProductoDTO.fromEntity(productoActualizado);
        notificarActualizacionStock(dto);
        return dto;
    }

    // Ajusta el stock de una talla específica para un producto (ej. Camisetas) y sincroniza el stock total.
    public ProductoDTO ajustarStockPorTalla(Long productoId, String talla, AjustarStockDTO ajustarStockDto) {
        Producto producto = iProductoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        String tallaNormalizada = talla.trim().toUpperCase();
        ProductoTalla productoTalla = iProductoTallaRepository
                .findByProductoIdAndTallaIgnoreCase(productoId, tallaNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Talla '" + tallaNormalizada + "' no encontrada para el producto con ID: " + productoId));

        int stockTallaActual = productoTalla.getStock() != null ? productoTalla.getStock() : 0;
        int cantidad = ajustarStockDto.getCantidad();
        String operacion = ajustarStockDto.getOperacion().trim().toUpperCase();

        if ("INCREMENTAR".equals(operacion)) {
            productoTalla.setStock(stockTallaActual + cantidad);
        } else if ("DECREMENTAR".equals(operacion)) {
            if (stockTallaActual - cantidad < 0) {
                throw new BadRequestException("Stock insuficiente para la talla " + tallaNormalizada);
            }
            productoTalla.setStock(stockTallaActual - cantidad);
        } else {
            throw new BadRequestException("Operación no válida. Use INCREMENTAR o DECREMENTAR.");
        }

        iProductoTallaRepository.save(productoTalla);

        // Sincroniza el stock total del producto sumando el stock de todas sus tallas
        int nuevoStockTotal = producto.getTallas().stream()
                .mapToInt(pt -> pt.getStock() != null ? pt.getStock() : 0)
                .sum();
        producto.setStock(nuevoStockTotal);

        Producto productoActualizado = iProductoRepository.save(producto);
        ProductoDTO dto = ProductoDTO.fromEntity(productoActualizado);
        notificarActualizacionStock(dto);
        return dto;
    }

    // Aplica borrado lógico deshabilitando el producto sin eliminar su historial de ventas.
    public ProductoDTO desactivarProducto(Long id) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setActivo(false);

        Producto productoDesactivado = iProductoRepository.save(producto);
        ProductoDTO dto = ProductoDTO.fromEntity(productoDesactivado);
        notificarActualizacionStock(dto);
        return dto;
    }

    // Vuelve a habilitar un producto descatalogado para que esté disponible nuevamente en el catálogo.
    public ProductoDTO reactivarProducto(Long id) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        producto.setActivo(true);

        Producto productoReactivado = iProductoRepository.save(producto);
        ProductoDTO dto = ProductoDTO.fromEntity(productoReactivado);
        notificarActualizacionStock(dto);
        return dto;
    }

    private boolean parsearEsPersonalizable(String texto) {
        return texto != null && (texto.equalsIgnoreCase("Sí") || texto.equalsIgnoreCase("Si"));
    }

    private boolean esCategoriaCamisetas(Categoria categoria) {
        return categoria != null && categoria.getNombre() != null
                && categoria.getNombre().trim().equalsIgnoreCase("Camisetas");
    }

    // Agrega una nueva talla o actualiza su stock si ya existe
    public ProductoTalla agregarTallaAProducto(Long productoId, String tallaNombre, Integer stock) {
        Producto producto = iProductoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        String tallaLimpia = tallaNombre.trim().toUpperCase();
        Optional<ProductoTalla> tallaExistente = iProductoTallaRepository.findByProductoIdAndTallaIgnoreCase(productoId, tallaLimpia);

        ProductoTalla talla;
        if (tallaExistente.isPresent()) {
            talla = tallaExistente.get();
            talla.setStock(stock);
        } else {
            talla = new ProductoTalla();
            talla.setProducto(producto);
            talla.setTalla(tallaLimpia);
            talla.setStock(stock);
            producto.getTallas().add(talla);
        }

        ProductoTalla guardada = iProductoTallaRepository.save(talla);

        // Mantiene el stock global del producto sincronizado
        int nuevoTotal = producto.getTallas().stream()
                .mapToInt(pt -> pt.getStock() != null ? pt.getStock() : 0)
                .sum();
        producto.setStock(nuevoTotal);
        Producto productoActualizado = iProductoRepository.save(producto);
        notificarActualizacionStock(ProductoDTO.fromEntity(productoActualizado));

        return guardada;
    }

    // Actualiza el stock de una talla concreta
    public ProductoTalla actualizarStockTalla(Long productoId, String tallaNombre, Integer nuevoStock) {
        if (nuevoStock < 0) {
            throw new BadRequestException("El stock no puede ser negativo.");
        }

        String tallaLimpia = tallaNombre.trim().toUpperCase();
        Producto producto = iProductoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        ProductoTalla talla = iProductoTallaRepository.findByProductoIdAndTallaIgnoreCase(productoId, tallaLimpia)
                .orElseThrow(() -> new ResourceNotFoundException("Talla " + tallaLimpia + " no encontrada para el producto " + productoId));

        talla.setStock(nuevoStock);
        ProductoTalla guardada = iProductoTallaRepository.save(talla);

        // Recalcula el total del producto
        int nuevoTotal = producto.getTallas().stream()
                .mapToInt(pt -> pt.getStock() != null ? pt.getStock() : 0)
                .sum();
        producto.setStock(nuevoTotal);
        Producto productoActualizado = iProductoRepository.save(producto);
        notificarActualizacionStock(ProductoDTO.fromEntity(productoActualizado));

        return guardada;
    }
}
