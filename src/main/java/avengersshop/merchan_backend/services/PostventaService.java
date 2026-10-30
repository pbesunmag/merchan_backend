package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.CambiarProductoDTO;
import avengersshop.merchan_backend.dto.request.DevolverLineaDTO;
import avengersshop.merchan_backend.dto.request.ItemCambioDestinoDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // Devolución parcial: repone y reembolsa solo las unidades indicadas de una línea concreta.
    // Si se devuelve todo lo que queda del pedido, se delega en la devolución total (estado DEVUELTO).
    public PedidoDTO devolverLinea(Long pedidoId, DevolverLineaDTO dto) {
        Pedido pedido = obtenerPedido(pedidoId);

        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new BadRequestException("Solo se pueden devolver pedidos en estado ENTREGADO. Estado actual: " + pedido.getEstado());
        }

        PedidoProducto linea = pedido.getProductos().stream()
                .filter(l -> l.getId().equals(dto.getLineaPedidoId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La línea " + dto.getLineaPedidoId() + " no pertenece al pedido " + pedidoId));

        int cantidad = dto.getCantidad();
        if (cantidad > linea.getCantidad()) {
            throw new BadRequestException("No se pueden devolver más unidades (" + cantidad
                    + ") de las compradas en la línea (" + linea.getCantidad() + ").");
        }

        if (pedido.getProductos().size() == 1 && cantidad == linea.getCantidad()) {
            return devolverPedido(pedidoId);
        }

        BigDecimal importe = linea.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
        Producto actualizado = reponerStock(linea.getProducto(), linea.getTalla(), cantidad);
        notificarActualizacionStock(actualizado);

        cajaService.registrarMovimiento(
                TipoMovimientoCaja.DEVOLUCION,
                importe.negate(),
                "Devolución parcial del pedido " + pedido.getCodigo() + ": " + cantidad + "x "
                        + linea.getProducto().getNombre(),
                pedido.getId());

        if (cantidad == linea.getCantidad()) {
            pedido.getProductos().remove(linea); // orphanRemoval elimina la línea vacía
        } else {
            linea.setCantidad(linea.getCantidad() - cantidad);
        }

        return PedidoDTO.fromEntity(iPedidoRepository.save(pedido));
    }

    // Cambio de una línea por uno o varios artículos del catálogo (cesta de cambio); ajusta stock y caja según diferencia.
    public PedidoDTO cambiarProducto(Long pedidoId, CambiarProductoDTO dto) {
        Pedido pedido = obtenerPedido(pedidoId);

        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new BadRequestException("Solo se pueden gestionar cambios sobre pedidos en estado ENTREGADO. Estado actual: " + pedido.getEstado());
        }

        PedidoProducto linea = pedido.getProductos().stream()
                .filter(l -> l.getId().equals(dto.getLineaPedidoId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La línea " + dto.getLineaPedidoId() + " no pertenece al pedido " + pedidoId));

        Integer cantEntregadaDto = dto.getCantidadEntregada() != null ? dto.getCantidadEntregada() : dto.getCantidad();
        if (cantEntregadaDto == null || cantEntregadaDto <= 0 || cantEntregadaDto > linea.getCantidad()) {
            throw new BadRequestException("No se pueden cambiar más unidades (" + cantEntregadaDto
                    + ") de las compradas en la línea (" + linea.getCantidad() + ").");
        }
        int cantidadEntregada = cantEntregadaDto;

        // Normalizar la lista de artículos de destino (admite cesta múltiple o formato 1 a 1)
        List<ItemCambioDestinoDTO> itemsDestino = new ArrayList<>();
        if (dto.getNuevosArticulos() != null && !dto.getNuevosArticulos().isEmpty()) {
            itemsDestino.addAll(dto.getNuevosArticulos());
        } else if (dto.getNuevoProductoId() != null) {
            int cantNueva = dto.getCantidadNueva() != null && dto.getCantidadNueva() > 0
                    ? dto.getCantidadNueva()
                    : cantidadEntregada;
            itemsDestino.add(new ItemCambioDestinoDTO(dto.getNuevoProductoId(), cantNueva, dto.getNuevaTalla()));
        } else {
            throw new BadRequestException("Debe seleccionar al menos un artículo para realizar el cambio.");
        }

        Producto productoOriginal = linea.getProducto();
        String tallaOriginal = linea.getTalla();
        BigDecimal precioAntiguo = linea.getPrecioUnitario();
        BigDecimal totalAntiguo = precioAntiguo.multiply(BigDecimal.valueOf(cantidadEntregada));

        // 1. Reponer primero el stock del producto saliente
        Producto originalActualizado = reponerStock(productoOriginal, tallaOriginal, cantidadEntregada);
        notificarActualizacionStock(originalActualizado);

        // 2. Reducir o eliminar la línea original entregada
        if (cantidadEntregada == linea.getCantidad()) {
            pedido.getProductos().remove(linea);
        } else {
            linea.setCantidad(linea.getCantidad() - cantidadEntregada);
        }

        // 3. Procesar cada artículo de la cesta de destino: descontar stock, acumular total y actualizar líneas
        BigDecimal totalNuevo = BigDecimal.ZERO;
        List<String> resumenNuevos = new ArrayList<>();

        for (ItemCambioDestinoDTO item : itemsDestino) {
            if (item.getProductoId() == null) {
                throw new BadRequestException("Cada artículo de destino debe indicar su productoId.");
            }
            int cantItem = item.getCantidad() != null ? item.getCantidad() : 1;
            if (cantItem <= 0) {
                throw new BadRequestException("La cantidad de cada artículo nuevo debe ser mayor que cero.");
            }

            Producto nuevoProducto = iProductoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + item.getProductoId()));
            if (!nuevoProducto.isActivo()) {
                throw new BadRequestException("El producto seleccionado no está activo: " + nuevoProducto.getNombre());
            }

            String nuevaTalla = item.getTalla() != null && !item.getTalla().isBlank()
                    ? item.getTalla().trim().toUpperCase()
                    : null;
            boolean tieneTallas = nuevoProducto.getTallas() != null && !nuevoProducto.getTallas().isEmpty();
            if (tieneTallas && nuevaTalla == null) {
                throw new BadRequestException("Debe especificar una talla para el producto: " + nuevoProducto.getNombre());
            }
            if (!tieneTallas) {
                nuevaTalla = null;
            }

            Producto nuevoActualizado = descontarStock(nuevoProducto, nuevaTalla, cantItem);
            notificarActualizacionStock(nuevoActualizado);

            BigDecimal precioNuevo = nuevoProducto.getPrecio();
            totalNuevo = totalNuevo.add(precioNuevo.multiply(BigDecimal.valueOf(cantItem)));
            resumenNuevos.add(cantItem + "x " + nuevoProducto.getNombre() + (nuevaTalla != null ? " (" + nuevaTalla + ")" : ""));

            // Si ya existe en el pedido una línea con el mismo producto y talla, sumar unidades; si no, añadir línea
            final String tallaFinal = nuevaTalla;
            Optional<PedidoProducto> lineaExistente = pedido.getProductos().stream()
                    .filter(l -> l.getProducto().getId().equals(nuevoProducto.getId())
                            && Objects.equals(l.getTalla(), tallaFinal))
                    .findFirst();

            if (lineaExistente.isPresent()) {
                PedidoProducto le = lineaExistente.get();
                le.setCantidad(le.getCantidad() + cantItem);
                le.setPrecioUnitario(precioNuevo);
            } else {
                PedidoProducto nuevaLinea = new PedidoProducto();
                nuevaLinea.setPedido(pedido);
                nuevaLinea.setProducto(nuevoProducto);
                nuevaLinea.setCantidad(cantItem);
                nuevaLinea.setPrecioUnitario(precioNuevo);
                nuevaLinea.setTalla(nuevaTalla);
                nuevaLinea.setTextoPersonalizado(nuevoProducto.isPersonalizable() ? linea.getTextoPersonalizado() : null);
                pedido.getProductos().add(nuevaLinea);
            }
        }

        // 4. Impacto económico en caja: totalNuevo - totalAntiguo
        BigDecimal diferencia = totalNuevo.subtract(totalAntiguo);
        String detalleCambio = String.join(", ", resumenNuevos);

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            cajaService.registrarMovimiento(
                    TipoMovimientoCaja.DIFERENCIA_CAMBIO,
                    diferencia,
                    "Abono de diferencia por cambio en comanda " + pedido.getCodigo() + ": "
                            + cantidadEntregada + "x " + productoOriginal.getNombre() + " -> " + detalleCambio,
                    pedido.getId());
        } else if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal montoReembolso = diferencia.abs();
            cajaService.registrarMovimiento(
                    TipoMovimientoCaja.DEVOLUCION,
                    montoReembolso.negate(),
                    "Reembolso de diferencia por cambio en comanda " + pedido.getCodigo() + ": "
                            + cantidadEntregada + "x " + productoOriginal.getNombre() + " -> " + detalleCambio,
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
