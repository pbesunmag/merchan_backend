package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.request.AgregarProductoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CambiarEstadoPedidoDTO;
import avengersshop.merchan_backend.dto.request.CheckoutPedidoDTO;
import avengersshop.merchan_backend.dto.request.CrearPedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.dto.response.ProductoDTO;
import avengersshop.merchan_backend.exceptions.BadRequestException;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.CuponDescuento;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import avengersshop.merchan_backend.models.PedidoProducto;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.models.ProductoTalla;
import avengersshop.merchan_backend.models.Promocion;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class) // Garantiza que las operaciones de escritura sean atómicas y manejen rollback automático ante excepciones
public class PedidoService {

    private static final String TOPIC_PRODUCTOS_STOCK = "/topic/productos-stock";

    private final IPedidoRepository iPedidoRepository;
    private final IProductoRepository iProductoRepository;
    private final IProductoTallaRepository iProductoTallaRepository;
    private final ITerminalRepository iTerminalRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CajaService cajaService;
    private final PromocionService promocionService;
    private final CuponDescuentoService cuponService;

    public PedidoService(IPedidoRepository iPedidoRepository,
                         IProductoRepository iProductoRepository,
                         IProductoTallaRepository iProductoTallaRepository,
                         ITerminalRepository iTerminalRepository,
                         SimpMessagingTemplate messagingTemplate,
                         CajaService cajaService,
                         PromocionService promocionService,
                         CuponDescuentoService cuponService) {
        this.iPedidoRepository = iPedidoRepository;
        this.iProductoRepository = iProductoRepository;
        this.iProductoTallaRepository = iProductoTallaRepository;
        this.iTerminalRepository = iTerminalRepository;
        this.messagingTemplate = messagingTemplate;
        this.cajaService = cajaService;
        this.promocionService = promocionService;
        this.cuponService = cuponService;
    }

    // Emite el nuevo stock por WebSocket. Si hay una transacción activa, se difiere hasta el commit
    // para no anunciar a los clientes un stock que podría revertirse con un rollback.
    private void notificarActualizacionStock(Producto producto) {
        if (messagingTemplate == null || producto == null) {
            return;
        }
        ProductoDTO dto = ProductoDTO.fromEntity(producto);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend(TOPIC_PRODUCTOS_STOCK, dto);
                }
            });
        } else {
            messagingTemplate.convertAndSend(TOPIC_PRODUCTOS_STOCK, dto);
        }
    }

    // Normaliza la talla recibida (null si viene vacía)
    private static String normalizarTalla(String talla) {
        return talla != null && !talla.isBlank() ? talla.trim().toUpperCase() : null;
    }

    // Normaliza el texto de grabado (null si viene vacío)
    private static String normalizarTexto(String texto) {
        return texto != null && !texto.isBlank() ? texto.trim() : null;
    }

    // Normaliza la variante (null si viene vacía; máximo 100 caracteres)
    private static String normalizarVariante(String variante) {
        if (variante == null || variante.isBlank()) {
            return null;
        }
        String limpia = variante.trim();
        return limpia.length() > 100 ? limpia.substring(0, 100) : limpia;
    }

    private static boolean tieneTallas(Producto producto) {
        return producto.getTallas() != null && !producto.getTallas().isEmpty();
    }

    // Suma exacta (BigDecimal) de precioUnitario * cantidad de todas las líneas del pedido
    private static BigDecimal calcularTotal(Pedido pedido) {
        return pedido.getProductos().stream()
                .filter(pp -> pp.getPrecioUnitario() != null && pp.getCantidad() != null)
                .map(pp -> pp.getPrecioUnitario().multiply(BigDecimal.valueOf(pp.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Asigna el código público AV-XXXX a partir del ID autogenerado.
    // El código temporal es único para no colisionar con el índice UNIQUE en altas concurrentes.
    private Pedido guardarConCodigo(Pedido pedido) {
        pedido.setCodigo("TMP-" + UUID.randomUUID());
        Pedido pedidoGuardado = iPedidoRepository.save(pedido);
        pedidoGuardado.setCodigo("AV-" + (1000 + pedidoGuardado.getId()));
        return pedidoGuardado;
    }

    // Stock disponible para un producto (por talla o general)
    private int stockDisponible(Producto producto, String talla) {
        if (talla != null) {
            return producto.getTallas().stream()
                    .filter(pt -> talla.equalsIgnoreCase(pt.getTalla()))
                    .findFirst()
                    .map(pt -> pt.getStock() != null ? pt.getStock() : 0)
                    .orElseThrow(() -> new ResourceNotFoundException("Talla '" + talla + "' no encontrada para el producto: " + producto.getNombre()));
        }
        return producto.getStock() != null ? producto.getStock() : 0;
    }

    // Descuenta stock (por talla o general) y recalcula el total. Lanza excepción si no hay unidades suficientes.
    private Producto descontarStock(Producto producto, String talla, int cantidad) {
        if (tieneTallas(producto) || talla != null) {
            if (talla == null) {
                throw new BadRequestException("Debe especificar una talla para el producto: " + producto.getNombre());
            }
            ProductoTalla productoTalla = iProductoTallaRepository
                    .findByProductoIdAndTallaIgnoreCase(producto.getId(), talla)
                    .orElseThrow(() -> new ResourceNotFoundException("Talla '" + talla + "' no encontrada para el producto: " + producto.getNombre()));

            int stockTalla = productoTalla.getStock() != null ? productoTalla.getStock() : 0;
            if (stockTalla < cantidad) {
                throw new BadRequestException("Stock insuficiente para la talla " + talla + " de " + producto.getNombre());
            }

            productoTalla.setStock(stockTalla - cantidad);
            iProductoTallaRepository.save(productoTalla);

            int nuevoStockTotal = producto.getTallas().stream()
                    .mapToInt(pt -> pt.getStock() != null ? pt.getStock() : 0)
                    .sum();
            producto.setStock(nuevoStockTotal);
        } else {
            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            if (stockActual < cantidad) {
                throw new BadRequestException("Stock insuficiente para " + producto.getNombre());
            }
            producto.setStock(stockActual - cantidad);
        }
        return iProductoRepository.save(producto);
    }

    // Repone al inventario las unidades de una línea (por talla o general)
    private Producto reponerStock(PedidoProducto linea) {
        Producto producto = linea.getProducto();
        if (linea.getTalla() != null) {
            iProductoTallaRepository.findByProductoIdAndTallaIgnoreCase(producto.getId(), linea.getTalla())
                    .ifPresent(pt -> {
                        pt.setStock((pt.getStock() != null ? pt.getStock() : 0) + linea.getCantidad());
                        iProductoTallaRepository.save(pt);
                    });
        }
        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        producto.setStock(stockActual + linea.getCantidad());
        return iProductoRepository.save(producto);
    }

    // Línea ya validada lista para persistirse en el checkout
    private record LineaValidada(Producto producto, String talla, int cantidad, String textoPersonalizado, String variante) { }

    /**
     * Checkout atómico: valida TODAS las líneas (existencia, activo, talla, personalización y stock agregado)
     * antes de escribir nada. Después crea el pedido, inserta las líneas con el precio oficial del catálogo,
     * descuenta el inventario y asienta la VENTA en caja. Cualquier excepción provoca rollback completo.
     */
    @Transactional(rollbackFor = Exception.class)
    public PedidoDTO checkout(CheckoutPedidoDTO checkoutDTO) {
        Terminal terminal = iTerminalRepository.findById(checkoutDTO.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("Terminal no encontrada con ID: " + checkoutDTO.getTerminalId()));

        // 1. Validación previa (sin escrituras) usando findById estándar de Spring Data JPA
        Map<Long, Producto> productos = new HashMap<>();
        Map<String, Integer> demandaPorArticulo = new LinkedHashMap<>();
        List<LineaValidada> lineasValidadas = new ArrayList<>();

        for (AgregarProductoPedidoDTO linea : checkoutDTO.getLineas()) {
            Producto producto = productos.computeIfAbsent(linea.getProductoId(), id ->
                    iProductoRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id)));

            if (!producto.isActivo()) {
                throw new BadRequestException("El producto '" + producto.getNombre() + "' no está activo.");
            }

            String talla = normalizarTalla(linea.getTalla());
            if (tieneTallas(producto) && talla == null) {
                throw new BadRequestException("Debe especificar una talla para el producto: " + producto.getNombre());
            }
            if (!tieneTallas(producto) && talla != null) {
                throw new BadRequestException("El producto '" + producto.getNombre() + "' no se vende por tallas.");
            }

            String texto = normalizarTexto(linea.getTextoPersonalizado());
            if (texto != null && !producto.isPersonalizable()) {
                throw new BadRequestException("El producto '" + producto.getNombre() + "' no admite personalización.");
            }

            demandaPorArticulo.merge(producto.getId() + "|" + Objects.toString(talla, ""), linea.getCantidad(), Integer::sum);
            lineasValidadas.add(new LineaValidada(producto, talla, linea.getCantidad(), texto, normalizarVariante(linea.getVariante())));
        }

        // Comprueba el stock contra la demanda total por producto/talla (cubre líneas repetidas)
        for (LineaValidada linea : lineasValidadas) {
            String clave = linea.producto().getId() + "|" + Objects.toString(linea.talla(), "");
            int solicitado = demandaPorArticulo.get(clave);
            int disponible = stockDisponible(linea.producto(), linea.talla());
            if (disponible < solicitado) {
                throw new BadRequestException("Stock insuficiente para " + linea.producto().getNombre()
                        + (linea.talla() != null ? " (talla " + linea.talla() + ")" : "")
                        + ": solicitadas " + solicitado + ", disponibles " + disponible + ".");
            }
        }

        // 2. Validación de cupón opcional antes de realizar escrituras
        CuponDescuento cuponAplicado = null;
        if (checkoutDTO.getCodigoCupon() != null && !checkoutDTO.getCodigoCupon().isBlank()) {
            cuponAplicado = cuponService.obtenerEntidadCuponValido(checkoutDTO.getCodigoCupon());
        }

        // 3. Escrituras: pedido, líneas e inventario (la venta se asienta en caja al confirmar el pago)
        Pedido pedido = new Pedido();
        pedido.setTerminal(terminal);
        pedido.setEstado(EstadoPedido.CREADO);
        pedido = guardarConCodigo(pedido);

        int pctCupon = cuponAplicado != null && cuponAplicado.getPorcentajeDescuento() != null
                ? cuponAplicado.getPorcentajeDescuento()
                : 0;

        Set<Producto> productosAfectados = new LinkedHashSet<>();
        for (LineaValidada linea : lineasValidadas) {
            descontarStock(linea.producto(), linea.talla(), linea.cantidad());
            productosAfectados.add(linea.producto());

            Producto prod = linea.producto();
            BigDecimal precioUnitarioFinal;
            if (Boolean.TRUE.equals(prod.getTieneDescuentoFijo())
                    && prod.getPorcentajeDescuento() != null
                    && prod.getPorcentajeDescuento() > 0) {
                precioUnitarioFinal = aplicarPorcentajeDescuento(prod.getPrecio(), prod.getPorcentajeDescuento());
            } else if (cuponAplicado != null && pctCupon > 0) {
                precioUnitarioFinal = aplicarPorcentajeDescuento(prod.getPrecio(), pctCupon);
            } else {
                precioUnitarioFinal = prod.getPrecio();
            }

            PedidoProducto nuevaLinea = new PedidoProducto();
            nuevaLinea.setPedido(pedido);
            nuevaLinea.setProducto(prod);
            nuevaLinea.setCantidad(linea.cantidad());
            nuevaLinea.setPrecioUnitario(precioUnitarioFinal); // Precio neto congelado en la línea
            nuevaLinea.setTalla(linea.talla());
            nuevaLinea.setTextoPersonalizado(linea.textoPersonalizado());
            nuevaLinea.setVariante(linea.variante());
            pedido.getProductos().add(nuevaLinea);
        }

        if (cuponAplicado != null) {
            cuponService.registrarUsoCupon(cuponAplicado);
        }

        Pedido pedidoGuardado = iPedidoRepository.save(pedido);

        // La venta NO se asienta aquí: el pedido queda en CREADO hasta confirmar el pago.
        productosAfectados.forEach(this::notificarActualizacionStock);

        return PedidoDTO.fromEntity(pedidoGuardado);
    }

    private BigDecimal aplicarPorcentajeDescuento(BigDecimal precioOriginal, int porcentaje) {
        if (precioOriginal == null) {
            return BigDecimal.ZERO;
        }
        if (porcentaje <= 0) {
            return precioOriginal.setScale(2, RoundingMode.HALF_UP);
        }
        int pctSeguro = Math.min(100, porcentaje);
        return precioOriginal
                .multiply(BigDecimal.valueOf(100 - pctSeguro))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transactional(rollbackFor = Exception.class)
    public PedidoDTO procesarCheckout(CheckoutPedidoDTO checkoutDTO) {
        return checkout(checkoutDTO);
    }

    // Crear nuevo pedido con generación automática de código
    public PedidoDTO crearPedido(CrearPedidoDTO crearPedidoDTO) {
        Terminal terminal = iTerminalRepository.findById(crearPedidoDTO.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("Terminal no encontrada con ID: " + crearPedidoDTO.getTerminalId()));

        Pedido pedido = new Pedido();
        pedido.setTerminal(terminal);
        pedido.setEstado(EstadoPedido.CREADO);

        return PedidoDTO.fromEntity(iPedidoRepository.save(guardarConCodigo(pedido)));
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
        String tallaSolicitada = normalizarTalla(agregarProductoPedidoDTO.getTalla());

        Producto productoActualizado = descontarStock(producto, tallaSolicitada, cantidadSolicitada);
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
            int pctPromo = promocionService.obtenerEntidadPromocionVigente()
                    .map(Promocion::getPorcentajeDescuento)
                    .orElse(0);
            int pctFijo = Boolean.TRUE.equals(producto.getTieneDescuentoFijo()) && producto.getPorcentajeDescuento() != null
                    ? producto.getPorcentajeDescuento()
                    : 0;
            BigDecimal precioUnitarioFinal = aplicarPorcentajeDescuento(producto.getPrecio(), Math.max(pctFijo, pctPromo));

            PedidoProducto nuevaLinea = new PedidoProducto();
            nuevaLinea.setPedido(pedido);
            nuevaLinea.setProducto(producto);
            nuevaLinea.setCantidad(cantidadSolicitada);
            nuevaLinea.setPrecioUnitario(precioUnitarioFinal);
            nuevaLinea.setTalla(tallaSolicitada);
            nuevaLinea.setTextoPersonalizado(agregarProductoPedidoDTO.getTextoPersonalizado());
            nuevaLinea.setVariante(normalizarVariante(agregarProductoPedidoDTO.getVariante()));
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

        notificarActualizacionStock(reponerStock(lineaAEliminar));

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

        // Cancelación: se repone el inventario y, si la venta ya estaba asentada, se anula en caja
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            pedido.getProductos().forEach(linea -> notificarActualizacionStock(reponerStock(linea)));
            anularVentaAsentada(pedido, "Anulación por cancelación del pedido ");
        }

        // Revertir a CREADO: el pago deja de estar confirmado, se anula la venta asentada
        if (nuevoEstado == EstadoPedido.CREADO) {
            anularVentaAsentada(pedido, "Anulación por reversión a CREADO del pedido ");
        }

        // Confirmación de pago: la venta entra en caja solo cuando el operario confirma el cobro
        // (también al entregar pedidos heredados sin pago registrado)
        if (nuevoEstado == EstadoPedido.PAGO_ACEPTADO || nuevoEstado == EstadoPedido.ENTREGADO) {
            registrarVentaSiProcede(pedido);
        }

        return PedidoDTO.fromEntity(iPedidoRepository.save(pedido));
    }

    // Asienta la venta del pedido en caja (saldo actual + total) si aún no hay una venta vigente
    private void registrarVentaSiProcede(Pedido pedido) {
        if (cajaService.ventaNetaPedido(pedido.getId()).signum() > 0) {
            return;
        }
        cajaService.registrarMovimiento(TipoMovimientoCaja.VENTA, calcularTotal(pedido),
                "Venta del pedido " + pedido.getCodigo(), pedido.getId());
    }

    // Anula en caja la venta vigente del pedido, si la hay
    private void anularVentaAsentada(Pedido pedido, String motivo) {
        BigDecimal neto = cajaService.ventaNetaPedido(pedido.getId());
        if (neto.signum() > 0) {
            cajaService.registrarMovimiento(TipoMovimientoCaja.DEVOLUCION, neto.negate(),
                    motivo + pedido.getCodigo(), pedido.getId());
        }
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