package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CrearCategoriaDTO;
import avengersshop.merchan_backend.dto.request.CrearProductoDTO;
import avengersshop.merchan_backend.dto.request.CrearTerminalDTO;
import avengersshop.merchan_backend.dto.response.CategoriaDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.dto.response.TerminalDTO;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.Categoria;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.models.Terminal;
import avengersshop.merchan_backend.repositories.ICategoriaRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import avengersshop.merchan_backend.repositories.ITerminalRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class AvengersShopService {

    private final ITerminalRepository iTerminalRepository;
    private final ICategoriaRepository iCategoriaRepository;
    private final IProductoRepository iProductoRepository;

    public AvengersShopService(ITerminalRepository iTerminalRepository,
                               ICategoriaRepository iCategoriaRepository,
                               IProductoRepository iProductoRepository) {
        this.iTerminalRepository = iTerminalRepository;
        this.iCategoriaRepository = iCategoriaRepository;
        this.iProductoRepository = iProductoRepository;
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

    //MÉTODOS DE PRODUCTOS
    //Listamos productos con filtros opcionales y ordenacion por precio
    public List<ProductoDTO> listarProductos(Boolean activos, Long idCategoria, String ordenacion, String tipoOrdenacion) {
        Stream<Producto> streamProducto = iProductoRepository.findAll().stream();
        //Filtro por activos
        if (activos != null){
            streamProducto = streamProducto.filter(p -> p.isActivo() == activos);
        }

        //Filtro por ID de categoría
        if (idCategoria != null){
            streamProducto = streamProducto.filter(p -> p.getCategoria() != null && p.getCategoria().getId().equals(idCategoria));
        }

        //Ordenación por precio si se solicita
        if("precio".equalsIgnoreCase(ordenacion)){
            if("DESC".equalsIgnoreCase(tipoOrdenacion)){
                //Ordena de mayor a menor el precio
                streamProducto = streamProducto.sorted(Comparator.comparing(Producto::getPrecio).reversed());
            } else {
                //Ordena de menor a mayor el precio
                streamProducto = streamProducto.sorted(Comparator.comparing(Producto::getPrecio));
            }
        }
        return streamProducto.map(ProductoDTO::fromEntity).toList();
    }

    //Creamos producto
    public ProductoDTO crearProducto(CrearProductoDTO crearProductoDto) {
        //Buscamos la categoría en BD. Si no existe, lanzamos la excepción personalizada de 404 NOT FOUND
        Categoria categoria = iCategoriaRepository.findById(crearProductoDto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + crearProductoDto.getCategoriaId()));
        Producto producto = new Producto();
        producto.setNombre(crearProductoDto.getNombre());
        producto.setDescripcion(crearProductoDto.getDescripcion());
        producto.setPersonaje(crearProductoDto.getPersonaje());
        producto.setPrecio(crearProductoDto.getPrecio());

        //Convertimos "Si"(con tilde) / "Si" (sin tilde) a boolean
        boolean esPersonalizable = crearProductoDto.getPersonalizable() != null &&
                (crearProductoDto.getPersonalizable().equalsIgnoreCase("Sí") ||
                        crearProductoDto.getPersonalizable().equalsIgnoreCase("Si"));

        producto.setPersonalizable(esPersonalizable);
        producto.setCategoria(categoria);
        producto.setActivo(true); //Al crear el producto se marca como activo por defecto

        Producto productoGuardado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoGuardado);
    }

    //Actualizamos producto existente
    public ProductoDTO actualizarProducto (Long id, CrearProductoDTO crearProductoDto) {

        //Buscamos si existe el producto a modificar
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        //Buscamos la nueva categoría
        Categoria categoria = iCategoriaRepository.findById(crearProductoDto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + crearProductoDto.getCategoriaId()));

        producto.setNombre(crearProductoDto.getNombre());
        producto.setDescripcion(crearProductoDto.getDescripcion());
        producto.setPersonaje(crearProductoDto.getPersonaje());
        producto.setPrecio(crearProductoDto.getPrecio());

        boolean esPersonalizable = crearProductoDto.getPersonalizable() != null &&
                (crearProductoDto.getPersonalizable().equalsIgnoreCase("Sí") ||
                        crearProductoDto.getPersonalizable().equalsIgnoreCase("Si"));

        producto.setPersonalizable(esPersonalizable);
        producto.setCategoria(categoria);

        Producto productoActualizado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoActualizado);
    }

    //Descativamos producto
    public ProductoDTO desactivarProducto(Long id) {
        Producto producto = iProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Desactivamos cambiando su estado a inactivo
        producto.setActivo(false);

        Producto productoDesactivado = iProductoRepository.save(producto);
        return ProductoDTO.fromEntity(productoDesactivado);
    }

}
