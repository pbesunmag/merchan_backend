package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CambiarProductoDTO;
import avengersshop.merchan_backend.dto.response.MetricasCajaDTO;
import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import avengersshop.merchan_backend.models.PedidoProducto;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.models.ProductoTalla;
import avengersshop.merchan_backend.models.TipoMovimientoCaja;
import avengersshop.merchan_backend.repositories.IPedidoRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import avengersshop.merchan_backend.repositories.IProductoTallaRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

// Gestión contable y de postventa: devoluciones, cambios de producto y métricas de beneficio.
@Service
@Transactional // Todas las operaciones (stock + caja + estado) son atómicas: si algo falla, se revierte todo
public class PostventaService {

    private static final String TOPIC_PRODUCTOS_STOCK = "/topic/productos-stock";

    private final IPedidoRepository iPedidoRepository;
    private final IProductoRepository iProductoRepository;
    private final IProductoTallaRepository iProductoTallaRepository;
    private final CajaService cajaService;
    private final SimpMessagingTemplate messagingTemplate;

    public PostventaService(IPedidoRepository iPedidoRepository,
                            IProductoRepository iProductoRepository,
                            IProductoTallaRepository iProductoTallaRepository,
                            CajaService cajaService,
                            SimpMessagingTemplate messagingTemplate) {
        this.iPedidoRepository = iPedidoRepository;
        this.iProductoRepository = iProductoRepository;
        this.iProductoTallaRepository = iProductoTallaRepository;
        this.cajaService = cajaService;
        this.messagingTemplate = messagingTemplate;
    }

    // Devolución total: repone stock, reembolsa el total pagado y deja el pedido en DEVUELTO.
    public PedidoDTO devolverPedido(Long pedidoId) {
        Pedido pedido = obtenerPedido(pedidoId);

        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new BadRequestException("Solo se pueden devolver pedidos en estado ENTREGADO. Estado actual: " + pedido.getEstado());
        }

        BigDecimal totalPedido = calcularTotal(pedido);

        for (PedidoProducto linea : pedido.getProductos()) {
            Producto actualizado = reponerStock(linea.getProducto(), linea.getTalla(), linea.getCantidad());
            notificarActualizacionStock(actualizado);
        }

        cajaService.registrarMovimiento(
                TipoMovimientoCaja.DEVOLUCION,
                totalPedido.negate(),
                "Devolución del pedido " + pedido.getCodigo(),
                pedido.getId());

        pedido.setEstado(EstadoPedido.DEVUELTO);
        return PedidoDTO.fromEntity(iPedidoRepository.save(pedido));
    }

    // Cambio de una línea por otro producto de igual o mayor valor; si es mayor, cobra la diferencia.
    public PedidoDTO cambiarProducto(Long pedidoId, CambiarProductoDTO dto) {
        Pedido pedido = obtenerPedido(pedidoId);

        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new BadRequestException("Solo se puede cambiar producto en pedidos ENTREGADOS. Estado actual: " + pedido.getEstado());
        }

        PedidoProducto linea = pedido.getProductos().stream()
                .filter(l -> l.getId().equals(dto.getLineaPedidoId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La línea " + dto.getLineaPedidoId() + " no pertenece al pedido " + pedidoId));

        int cantidad = dto.getCantidad();
        if (cantidad > linea.getCantidad()) {
            throw new BadRequestException("No se pueden cambiar más unidades (" + cantidad
                    + ") de las compradas en la línea (" + linea.getCantidad() + ").");
        }

        Producto nuevoProducto = iProductoRepository.findById(dto.getNuevoProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + dto.getNuevoProductoId()));
        if (!nuevoProducto.isActivo()) {
            throw new BadRequestException("El producto seleccionado no está activo.");
        }

        String nuevaTalla = dto.getNuevaTalla() != null && !dto.getNuevaTalla().isBlank()
                ? dto.getNuevaTalla().trim().toUpperCase()
                : null;
        boolean tieneTallas = nuevoProducto.getTallas() != null && !nuevoProducto.getTallas().isEmpty();
        if (tieneTallas && nuevaTalla == null) {
            throw new BadRequestException("Debe especificar una talla para este producto.");
        }

        Producto productoOriginal = linea.getProducto();
        String tallaOriginal = linea.getTalla();
        if (productoOriginal.getId().equals(nuevoProducto.getId())
                && java.util.Objects.equals(tallaOriginal, nuevaTalla)) {
            throw new BadRequestException("El nuevo producto y talla coinciden con los originales.");
        }

        // Política de empresa: solo igual o mayor valor
        BigDecimal precioAntiguo = linea.getPrecioUnitario();
        BigDecimal precioNuevo = nuevoProducto.getPrecio();
        if (precioNuevo.compareTo(precioAntiguo) < 0) {
            throw new BadRequestException("Por política de empresa, el cambio solo se permite por un producto de igual o mayor valor.");
        }
        BigDecimal diferencia = precioNuevo.subtract(precioAntiguo).multiply(BigDecimal.valueOf(cantidad));

        // Stock: primero se valida y descuenta el nuevo, luego se repone el saliente.
        // Si el nuevo no tiene stock se lanza excepción y se revierte la transacción.
        Producto nuevoActualizado = descontarStock(nuevoProducto, nuevaTalla, cantidad);
        Producto originalActualizado = reponerStock(productoOriginal, tallaOriginal, cantidad);
        notificarActualizacionStock(nuevoActualizado);
        notificarActualizacionStock(originalActualizado);

        // Texto personalizado solo si el nuevo producto lo admite
        String texto = nuevoProducto.isPersonalizable() ? linea.getTextoPersonalizado() : null;

        if (cantidad == linea.getCantidad()) {
            // Cambio de toda la línea: se sustituye en el sitio
            linea.setProducto(nuevoProducto);
            linea.setTalla(nuevaTalla);
            linea.setPrecioUnitario(precioNuevo);
            linea.setTextoPersonalizado(texto);
        } else {
            // Cambio parcial: se reducen las unidades de la línea original y se añade una nueva
            linea.setCantidad(linea.getCantidad() - cantidad);
            PedidoProducto nuevaLinea = new PedidoProducto();
            nuevaLinea.setPedido(pedido);
            nuevaLinea.setProducto(nuevoProducto);
            nuevaLinea.setCantidad(cantidad);
            nuevaLinea.setPrecioUnitario(precioNuevo);
            nuevaLinea.setTalla(nuevaTalla);
            nuevaLinea.setTextoPersonalizado(texto);
            pedido.getProductos().add(nuevaLinea);
        }

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            cajaService.registrarMovimiento(
                    TipoMovimientoCaja.DIFERENCIA_CAMBIO,
                    diferencia,
                    "Diferencia por cambio en pedido " + pedido.getCodigo() + ": "
                            + productoOriginal.getNombre() + " -> " + nuevoProducto.getNombre(),
                    pedido.getId());
        }

        return PedidoDTO.fromEntity(iPedidoRepository.save(pedido));
    }

    // Métricas contables del periodo (dia, mes o anio).
    @Transactional(readOnly = true)
    public MetricasCajaDTO obtenerMetricasBeneficio(String periodo) {
        return cajaService.calcularMetricas(periodo);
    }

    private Pedido obtenerPedido(Long pedidoId) {
        return iPedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + pedidoId));
    }

    private BigDecimal calcularTotal(Pedido pedido) {
        return pedido.getProductos().stream()
                .filter(pp -> pp.getPrecioUnitario() != null && pp.getCantidad() != null)
                .map(pp -> pp.getPrecioUnitario().multiply(BigDecimal.valueOf(pp.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Suma unidades al stock de la talla (si hay) y recalcula el stock total del producto.
    private Producto reponerStock(Producto producto, String talla, int cantidad) {
        if (talla != null) {
            ProductoTalla productoTalla = iProductoTallaRepository
                    .findByProductoIdAndTallaIgnoreCase(producto.getId(), talla)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Talla '" + talla + "' no encontrada para el producto: " + producto.getNombre()));
            productoTalla.setStock((productoTalla.getStock() != null ? productoTalla.getStock() : 0) + cantidad);
            iProductoTallaRepository.save(productoTalla);
            producto.setStock(sumarStockTallas(producto));
        } else {
            producto.setStock((producto.getStock() != null ? producto.getStock() : 0) + cantidad);
        }
        return iProductoRepository.save(producto);
    }

    // Resta unidades al stock validando disponibilidad.
    private Producto descontarStock(Producto producto, String talla, int cantidad) {
        if (talla != null) {
            ProductoTalla productoTalla = iProductoTallaRepository
                    .findByProductoIdAndTallaIgnoreCase(producto.getId(), talla)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Talla '" + talla + "' no encontrada para el producto: " + producto.getNombre()));
            int stockTalla = productoTalla.getStock() != null ? productoTalla.getStock() : 0;
            if (stockTalla < cantidad) {
                throw new BadRequestException("Stock insuficiente para la talla " + talla);
            }
            productoTalla.setStock(stockTalla - cantidad);
            iProductoTallaRepository.save(productoTalla);
            producto.setStock(sumarStockTallas(producto));
        } else {
            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            if (stockActual < cantidad) {
                throw new BadRequestException("Stock insuficiente");
            }
            producto.setStock(stockActual - cantidad);
        }
        return iProductoRepository.save(producto);
    }

    private int sumarStockTallas(Producto producto) {
        return producto.getTallas().stream()
                .mapToInt(pt -> pt.getStock() != null ? pt.getStock() : 0)
                .sum();
    }

    private void notificarActualizacionStock(Producto producto) {
        if (messagingTemplate != null && producto != null) {
            messagingTemplate.convertAndSend(TOPIC_PRODUCTOS_STOCK, ProductoDTO.fromEntity(producto));
        }
    }
}
