package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.AgregarProductoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CambiarEstadoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CrearPedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import avengersshop.merchan_backend.models.PedidoProducto;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.models.Terminal;
import avengersshop.merchan_backend.repositories.IPedidoRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import avengersshop.merchan_backend.repositories.ITerminalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PedidoService {

    private final IPedidoRepository iPedidoRepository;
    private final IProductoRepository iProductoRepository;
    private final ITerminalRepository iTerminalRepository;

    public PedidoService(IPedidoRepository iPedidoRepository,
                         IProductoRepository iProductoRepository,
                         ITerminalRepository iTerminalRepository) {
        this.iPedidoRepository = iPedidoRepository;
        this.iProductoRepository = iProductoRepository;
        this.iTerminalRepository = iTerminalRepository;
    }

    // 1. Crear nuevo pedido con generación automática de código
    public PedidoDTO crearPedido(CrearPedidoDTO crearPedidoDTO) {
        Terminal terminal = iTerminalRepository.findById(crearPedidoDTO.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("Terminal no encontrada con ID: " + crearPedidoDTO.getTerminalId()));

        Pedido pedido = new Pedido();
        pedido.setTerminal(terminal);
        pedido.setEstado(EstadoPedido.CREADO);
        pedido.setCodigo("PENDIENTE"); // Código temporal para guardar y generar ID

        // La fecha de creación se gestiona automáticamente con @EnableJpaAuditing
        Pedido pedidoGuardado = iPedidoRepository.save(pedido);

        // Generamos el código público oficial
        String codigoGenerado = "AV-" + (1000 + pedidoGuardado.getId());
        pedidoGuardado.setCodigo(codigoGenerado);

        return PedidoDTO.fromEntity(iPedidoRepository.save(pedidoGuardado));
    }

    // 2. Añadir producto a un pedido
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

        // Si el producto ya existe en el pedido, incrementamos cantidad
        Optional<PedidoProducto> lineaExistente = pedido.getProductos().stream()
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
            pedido.getProductos().add(nuevaLinea);
        }

        Pedido pedidoActualizado = iPedidoRepository.save(pedido);
        return PedidoDTO.fromEntity(pedidoActualizado);
    }

    // 3. Eliminar producto de un pedido
    public PedidoDTO eliminarProductoDePedido(Long pedidoId, Long productoId) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() != EstadoPedido.CREADO) {
            throw new BadRequestException("No se pueden eliminar productos de un pedido en estado: " + pedido.getEstado());
        }

        boolean removido = pedido.getProductos().removeIf(l -> l.getProducto().getId().equals(productoId));

        if (!removido) {
            throw new ResourceNotFoundException("El producto con ID " + productoId + " no está en este pedido.");
        }

        Pedido pedidoActualizado = iPedidoRepository.save(pedido);
        return PedidoDTO.fromEntity(pedidoActualizado);
    }

    // 4. Cambiar estado del pedido
    public PedidoDTO cambiarEstadoPedido(Long pedidoId, CambiarEstadoPedidoDTO cambiarEstadoPedidoDTO) {
        Pedido pedido = iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        pedido.setEstado(cambiarEstadoPedidoDTO.getEstadoPedido());
        return PedidoDTO.fromEntity(iPedidoRepository.save(pedido));
    }

    // 5. Listar pedidos paginados y filtrados
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPedidos(EstadoPedido estado, Pageable pageable) {
        Page<Pedido> pedidos = iPedidoRepository.buscarConFiltroEstado(estado, pageable);
        return pedidos.map(PedidoDTO::fromEntity);
    }

    // 6. Obtener pedido por código único
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPorCodigo(String codigo) {
        Pedido pedido = iPedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con código: " + codigo));
        return PedidoDTO.fromEntity(pedido);
    }

    // 7. Listar pedidos para la pantalla del local
    @Transactional(readOnly = true)
    public List<PedidoPantallaDTO> listarPedidosPantalla() {
        List<Pedido> pedidos = iPedidoRepository.findAllByOrderByFechaCreacionAsc();
        return pedidos.stream()
                .map(PedidoPantallaDTO::fromEntity)
                .toList();
    }
}