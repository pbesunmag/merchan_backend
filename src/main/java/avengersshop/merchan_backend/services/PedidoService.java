package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.AgregarProductoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CambiarEstadoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CrearPedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import avengersshop.merchan_backend.models.PedidoProducto;
import avengersshop.merchan_backend.models.Terminal;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.repositories.IPedidoProductoRepository;
import avengersshop.merchan_backend.repositories.IPedidoRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import avengersshop.merchan_backend.repositories.ITerminalRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final IPedidoRepository iPedidoRepository;
    private final IPedidoProductoRepository iPedidoProductoRepository;
    private final IProductoRepository iProductoRepository;
    private final ITerminalRepository iTerminalRepository;

    public PedidoService(IPedidoRepository iPedidoRepository,
                         IPedidoProductoRepository iPedidoProductoRepository,
                         IProductoRepository iProductoRepository,
                         ITerminalRepository iTerminalRepository) {
        this.iPedidoRepository = iPedidoRepository;
        this.iPedidoProductoRepository = iPedidoProductoRepository;
        this.iProductoRepository = iProductoRepository;
        this.iTerminalRepository = iTerminalRepository;
    }
    // @Transactional garantiza el "Todo o Nada": si ocurre un error/excepción durante la ejecución,
    // Spring realiza un ROLLBACK automático para no dejar datos corruptos en MySQL.
    // Además, mantiene la sesión de Hibernate abierta para poder cargar colecciones Lazy (como pedido.getLineas()).

    // Creamos nuevo pedido con generación automática de código
    @Transactional
    public PedidoDTO crearPedido(CrearPedidoDTO crearPedidoDTO) {
        Terminal terminal = iTerminalRepository.findById(crearPedidoDTO.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("Terminal no encontrada con ID: " + crearPedidoDTO.getTerminalId()));

        Pedido pedido = new Pedido();
        pedido.setTerminal(terminal);
        pedido.setEstado(EstadoPedido.CREADO);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setCodigo("PENDIENTE"); //Pendiente temporal para guardar y obtener el ID

        Pedido pedidoGuardado = iPedidoRepository.save(pedido);

        // Generamos un código público basado en el ID
        String codigoGenerado = "AV-" + (1000 + pedidoGuardado.getId());
        pedidoGuardado.setCodigo(codigoGenerado);

        return PedidoDTO.fromEntity(iPedidoRepository.save(pedidoGuardado));
    }

    //Añadimos producto a un pedido
    @Transactional
    public PedidoDTO agregarProductoAPedido(Long pedidoId, AgregarProductoPedidoDTO agregarProductoPedidoDTO) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        Producto producto = iProductoRepository.findById(agregarProductoPedidoDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + agregarProductoPedidoDTO.getProductoId()));

        if (!producto.isActivo()) {
            throw new IllegalArgumentException("El producto seleccionado no está activo.");
        }

        // Si el producto ya existe en el pedido, incrementamos cantidad
        Optional<PedidoProducto> lineaExistente = pedido.getLineas().stream()
                .filter(l -> l.getProducto().getId().equals(producto.getId()))
                .findFirst();

        if (lineaExistente.isPresent()) {
            PedidoProducto linea = lineaExistente.get();
            linea.setCantidad(linea.getCantidad() + agregarProductoPedidoDTO.getCantidad());
            if (agregarProductoPedidoDTO.getTextoPersonalizado() != null && !agregarProductoPedidoDTO.getTextoPersonalizado().isBlank()) {
                linea.setTextoPersonalizado(agregarProductoPedidoDTO.getTextoPersonalizado());
            }
        } else {
            PedidoProducto nuevaLinea = new PedidoProducto();
            nuevaLinea.setPedido(pedido);
            nuevaLinea.setProducto(producto);
            nuevaLinea.setCantidad(agregarProductoPedidoDTO.getCantidad());
            nuevaLinea.setPrecioUnitario(producto.getPrecio());
            nuevaLinea.setTextoPersonalizado(agregarProductoPedidoDTO.getTextoPersonalizado());
            pedido.getLineas().add(nuevaLinea);
        }

        Pedido pedidoActualizado = iPedidoRepository.save(pedido);
        return PedidoDTO.fromEntity(pedidoActualizado);
    }

    // Eliminamos producto de un pedido
    @Transactional
    public PedidoDTO eliminarProductoDePedido(Long pedidoId, Long productoId) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        boolean removido = pedido.getLineas().removeIf(l -> l.getProducto().getId().equals(productoId));

        if (!removido) {
            throw new ResourceNotFoundException("El producto con ID " + productoId + " no está en este pedido.");
        }

        Pedido pedidoActualizado = iPedidoRepository.save(pedido);
        return PedidoDTO.fromEntity(pedidoActualizado);
    }

    // Cambiamos estado del pedido
    @Transactional
    public PedidoDTO cambiarEstadoPedido(Long pedidoId, CambiarEstadoPedidoDTO cambiarEstadoPedidoDTO) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        pedido.setEstado(cambiarEstadoPedidoDTO.getEstadoPedido());
        return PedidoDTO.fromEntity(iPedidoRepository.save(pedido));
    }

    // Obtenemos pedido por código
    // Solo lectura: le avisa a la base de datos que solo vamos a consultar información, no a modificarla.
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPorCodigo(String codigo) {
        Pedido pedido = iPedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con código: " + codigo));

        return PedidoDTO.fromEntity(pedido);
    }

    // Listamos todos los pedidos o filtrados por estado
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPedidosPorEstado(EstadoPedido estado) {
        List<Pedido> pedidos;

        if (estado != null) {
            pedidos = iPedidoRepository.findByEstadoOrderByFechaCreacionAsc(estado);
        } else {
            pedidos = iPedidoRepository.findAllByOrderByFechaCreacionAsc();
        }

        return pedidos.stream().map(PedidoDTO::fromEntity).toList();
    }

    //Listamos pedidos para pantalla
    @Transactional(readOnly = true)
    public List<PedidoPantallaDTO> obtenerPedidosParaPantalla() {
        return iPedidoRepository.findAllByOrderByFechaCreacionAsc().stream()
                .map(PedidoPantallaDTO::fromEntity).toList();
    }


}
