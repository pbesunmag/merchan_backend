package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.AgregarProductoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CambiarEstadoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CrearPedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import avengersshop.merchan_backend.models.PedidoProducto;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.models.ProductoTalla;
import avengersshop.merchan_backend.models.Terminal;
import avengersshop.merchan_backend.models.TipoMovimientoCaja;
import avengersshop.merchan_backend.repositories.IPedidoRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import avengersshop.merchan_backend.repositories.IProductoTallaRepository;
import avengersshop.merchan_backend.repositories.ITerminalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional // Garantiza que las operaciones de escritura sean atómicas y manejen rollback automático ante excepciones
public class PedidoService {

    private static final String TOPIC_PRODUCTOS_STOCK = "/topic/productos-stock";

    private final IPedidoRepository iPedidoRepository;
    private final IProductoRepository iProductoRepository;
    private final IProductoTallaRepository iProductoTallaRepository;
    private final ITerminalRepository iTerminalRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CajaService cajaService;

    public PedidoService(IPedidoRepository iPedidoRepository,
                         IProductoRepository iProductoRepository,
                         IProductoTallaRepository iProductoTallaRepository,
                         ITerminalRepository iTerminalRepository,
                         SimpMessagingTemplate messagingTemplate,
                         CajaService cajaService) {
        this.iPedidoRepository = iPedidoRepository;
        this.iProductoRepository = iProductoRepository;
        this.iProductoTallaRepository = iProductoTallaRepository;
        this.iTerminalRepository = iTerminalRepository;
        this.messagingTemplate = messagingTemplate;
        this.cajaService = cajaService;
    }

    private void notificarActualizacionStock(Producto producto) {
        if (messagingTemplate != null && producto != null) {
            messagingTemplate.convertAndSend(TOPIC_PRODUCTOS_STOCK, ProductoDTO.fromEntity(producto));
        }
    }

    // Crear nuevo pedido con generación automática de código
    public PedidoDTO crearPedido(CrearPedidoDTO crearPedidoDTO) {
        Terminal terminal = iTerminalRepository.findById(crearPedidoDTO.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("Terminal no encontrada con ID: " + crearPedidoDTO.getTerminalId()));

        Pedido pedido = new Pedido();
        pedido.setTerminal(terminal);
        pedido.setEstado(EstadoPedido.CREADO);
        pedido.setCodigo("PENDIENTE");

        Pedido pedidoGuardado = iPedidoRepository.save(pedido);

        String codigoGenerado = "AV-" + (1000 + pedidoGuardado.getId());
        pedidoGuardado.setCodigo(codigoGenerado);

        return PedidoDTO.fromEntity(iPedidoRepository.save(pedidoGuardado));
    }

    // Añadir producto a un pedido descontando automáticamente el stock (por talla o general)
    public PedidoDTO agregarProductoAPedido(Long pedidoId, AgregarProductoPedidoDTO agregarProductoPedidoDTO) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() != EstadoPedido.CREADO) {
            throw new BadRequestException("No se pueden añadir productos a un pedido en estado: " + pedido.getEstado());
        }

        Producto producto = iProductoRepository.findById(agregarProductoPedidoDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + agregarProductoPedidoDTO.getProductoId()));
        if (!producto.isActivo()) {
            throw new BadRequestException("El producto seleccionado no está activo.");
        }

        int cantidadSolicitada = agregarProductoPedidoDTO.getCantidad();
        String tallaSolicitada = agregarProductoPedidoDTO.getTalla() != null && !agregarProductoPedidoDTO.getTalla().isBlank()
                ? agregarProductoPedidoDTO.getTalla().trim().toUpperCase()
                : null;

        boolean tieneTallas = producto.getTallas() != null && !producto.getTallas().isEmpty();

        Producto productoActualizado;
        if (tieneTallas || tallaSolicitada != null) {
            if (tallaSolicitada == null) {
                throw new BadRequestException("Debe especificar una talla para este producto.");
            }
            ProductoTalla productoTalla = iProductoTallaRepository
                    .findByProductoIdAndTallaIgnoreCase(producto.getId(), tallaSolicitada)
                    .orElseThrow(() -> new ResourceNotFoundException("Talla '" + tallaSolicitada + "' no encontrada para el producto: " + producto.getNombre()));

            int stockTalla = productoTalla.getStock() != null ? productoTalla.getStock() : 0;
            if (stockTalla < cantidadSolicitada) {
                throw new BadRequestException("Stock insuficiente para la talla " + tallaSolicitada);
            }

            productoTalla.setStock(stockTalla - cantidadSolicitada);
            iProductoTallaRepository.save(productoTalla);

            int nuevoStockTotal = producto.getTallas().stream()
                    .mapToInt(pt -> pt.getStock() != null ? pt.getStock() : 0)
                    .sum();
            producto.setStock(nuevoStockTotal);
            productoActualizado = iProductoRepository.save(producto);
        } else {
            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            if (stockActual < cantidadSolicitada) {
                throw new BadRequestException("Stock insuficiente");
            }
            producto.setStock(stockActual - cantidadSolicitada);
            productoActualizado = iProductoRepository.save(producto);
        }

        notificarActualizacionStock(productoActualizado);

        // Si el mismo producto y misma talla ya existen en el pedido, incrementamos cantidad
        Optional<PedidoProducto> lineaExistente = pedido.getProductos().stream()
                .filter(l -> l.getProducto().getId().equals(producto.getId())
                        && Objects.equals(l.getTalla(), tallaSolicitada))
                .findFirst();

        if (lineaExistente.isPresent()) {
            PedidoProducto linea = lineaExistente.get();
            linea.setCantidad(linea.getCantidad() + cantidadSolicitada);
            if (agregarProductoPedidoDTO.getTextoPersonalizado() != null && !agregarProductoPedidoDTO.getTextoPersonalizado().isBlank()) {
                linea.setTextoPersonalizado(agregarProductoPedidoDTO.getTextoPersonalizado());
            }
        } else {
            PedidoProducto nuevaLinea = new PedidoProducto();
            nuevaLinea.setPedido(pedido);
            nuevaLinea.setProducto(producto);
            nuevaLinea.setCantidad(cantidadSolicitada);
            nuevaLinea.setPrecioUnitario(producto.getPrecio());
            nuevaLinea.setTalla(tallaSolicitada);
            nuevaLinea.setTextoPersonalizado(agregarProductoPedidoDTO.getTextoPersonalizado());
            pedido.getProductos().add(nuevaLinea);
        }

        Pedido pedidoActualizado = iPedidoRepository.save(pedido);
        return PedidoDTO.fromEntity(pedidoActualizado);
    }

    // Eliminar producto de un pedido y restaurar su stock (por talla o general)
    public PedidoDTO eliminarProductoDePedido(Long pedidoId, Long productoId) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() != EstadoPedido.CREADO) {
            throw new BadRequestException("No se pueden eliminar productos de un pedido en estado: " + pedido.getEstado());
        }

        PedidoProducto lineaAEliminar = pedido.getProductos().stream()
                .filter(l -> l.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + productoId + " no está en este pedido."));

        Producto producto = lineaAEliminar.getProducto();
        if (lineaAEliminar.getTalla() != null) {
            iProductoTallaRepository.findByProductoIdAndTallaIgnoreCase(producto.getId(), lineaAEliminar.getTalla())
                    .ifPresent(pt -> {
                        pt.setStock((pt.getStock() != null ? pt.getStock() : 0) + lineaAEliminar.getCantidad());
                        iProductoTallaRepository.save(pt);
                    });
        }
        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        producto.setStock(stockActual + lineaAEliminar.getCantidad());
        Producto productoActualizado = iProductoRepository.save(producto);
        notificarActualizacionStock(productoActualizado);

        pedido.getProductos().remove(lineaAEliminar);

        Pedido pedidoActualizado = iPedidoRepository.save(pedido);
        return PedidoDTO.fromEntity(pedidoActualizado);
    }

    // Cambiar estado del pedido
    public PedidoDTO cambiarEstadoPedido(Long pedidoId, CambiarEstadoPedidoDTO cambiarEstadoPedidoDTO) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        EstadoPedido nuevoEstado = cambiarEstadoPedidoDTO.getEstadoPedido();
        if (nuevoEstado == EstadoPedido.DEVUELTO) {
            throw new BadRequestException("Para devolver un pedido use POST /api/pedidos/{id}/devolucion.");
        }
        if (!pedido.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new BadRequestException("Transición de estado no permitida: de "
                    + pedido.getEstado() + " a " + nuevoEstado);
        }

        pedido.setEstado(nuevoEstado);

        // Al entregar el pedido se registra el ingreso por venta en caja
        if (nuevoEstado == EstadoPedido.ENTREGADO
                && !cajaService.existeMovimiento(pedido.getId(), TipoMovimientoCaja.VENTA)) {
            BigDecimal total = pedido.getProductos().stream()
                    .filter(pp -> pp.getPrecioUnitario() != null && pp.getCantidad() != null)
                    .map(pp -> pp.getPrecioUnitario().multiply(BigDecimal.valueOf(pp.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cajaService.registrarMovimiento(TipoMovimientoCaja.VENTA, total,
                    "Venta del pedido " + pedido.getCodigo(), pedido.getId());
        }

        return PedidoDTO.fromEntity(iPedidoRepository.save(pedido));
    }

    // Listar pedidos paginados y filtrados
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPedidos(EstadoPedido estado, Pageable pageable) {
        Page<Pedido> pedidos = iPedidoRepository.buscarConFiltroEstado(estado, pageable);
        return pedidos.map(PedidoDTO::fromEntity);
    }

    // Obtener pedido por código único
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPorCodigo(String codigo) {
        Pedido pedido = iPedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con código: " + codigo));
        return PedidoDTO.fromEntity(pedido);
    }

    // Listar pedidos para la pantalla del local
    @Transactional(readOnly = true)
    public List<PedidoPantallaDTO> listarPedidosPantalla() {
        List<Pedido> pedidos = iPedidoRepository.findAllByOrderByFechaCreacionAsc();
        return pedidos.stream()
                .map(PedidoPantallaDTO::fromEntity)
                .toList();
    }
}
