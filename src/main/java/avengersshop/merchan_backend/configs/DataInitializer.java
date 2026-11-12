package avengersshop.merchan_backend.configs;

import avengersshop.merchan_backend.models.CuponDescuento;
import avengersshop.merchan_backend.models.MovimientoCaja;
import avengersshop.merchan_backend.models.Producto;
import avengersshop.merchan_backend.models.Promocion;
import avengersshop.merchan_backend.models.TipoMovimientoCaja;
import avengersshop.merchan_backend.repositories.CuponDescuentoRepository;
import avengersshop.merchan_backend.repositories.IMovimientoCajaRepository;
import avengersshop.merchan_backend.repositories.IProductoRepository;
import avengersshop.merchan_backend.repositories.PromocionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

// Si la caja, promociones o cupones están vacíos al arrancar, registra los datos semilla iniciales.
@Component
public class DataInitializer implements CommandLineRunner {

    private final IMovimientoCajaRepository movimientoCajaRepository;
    private final PromocionRepository promocionRepository;
    private final CuponDescuentoRepository cuponRepository;
    private final IProductoRepository productoRepository;

    public DataInitializer(IMovimientoCajaRepository movimientoCajaRepository,
                           PromocionRepository promocionRepository,
                           CuponDescuentoRepository cuponRepository,
                           IProductoRepository productoRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.promocionRepository = promocionRepository;
        this.cuponRepository = cuponRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (movimientoCajaRepository.count() == 0) {
            MovimientoCaja fondo = new MovimientoCaja();
            fondo.setTipoMovimiento(TipoMovimientoCaja.FONDO_INICIAL);
            fondo.setMonto(new BigDecimal("500.00"));
            fondo.setSaldoResultante(new BigDecimal("500.00"));
            fondo.setDescripcion("Apertura de caja base");
            fondo.setFecha(LocalDateTime.now());
            movimientoCajaRepository.save(fondo);
        }

        if (promocionRepository.count() == 0) {
            LocalDateTime ahora = LocalDateTime.now();
            int anioActual = ahora.getYear();

            List<Promocion> semillas = List.of(
                    Promocion.builder()
                            .codigo("HALLOWEEN_STARK")
                            .nombre("Protocolo Noche de Brujas Stark")
                            .descripcion("15% de descuento en todo el equipamiento táctico durante Halloween.")
                            .porcentajeDescuento(15)
                            .fechaInicio(LocalDateTime.of(anioActual, 10, 25, 0, 0))
                            .fechaFin(LocalDateTime.of(anioActual, 11, 2, 23, 59, 59))
                            .activo(false)
                            .aplicaATodo(true)
                            .build(),
                    Promocion.builder()
                            .codigo("BLACK_FRIDAY_WAR")
                            .nombre("Infinity Black Friday")
                            .descripcion("Chasquido de precios: 20% de descuento en todo el arsenal Avengers.")
                            .porcentajeDescuento(20)
                            .fechaInicio(LocalDateTime.of(anioActual, 11, 20, 0, 0))
                            .fechaFin(LocalDateTime.of(anioActual, 11, 30, 23, 59, 59))
                            .activo(true)
                            .aplicaATodo(true)
                            .build(),
                    Promocion.builder()
                            .codigo("AVENGERS_HOLIDAYS")
                            .nombre("Protocolo Fiestas Stark")
                            .descripcion("25% de descuento especial de Navidad y Año Nuevo en la Torre Avengers.")
                            .porcentajeDescuento(25)
                            .fechaInicio(LocalDateTime.of(anioActual, 12, 15, 0, 0))
                            .fechaFin(LocalDateTime.of(anioActual + 1, 1, 6, 23, 59, 59))
                            .activo(false)
                            .aplicaATodo(true)
                            .build(),
                    Promocion.builder()
                            .codigo("LOVE_YOU_3000")
                            .nombre("Especial San Valentín: Te Quiero 3000")
                            .descripcion("14% de descuento en regalos para tu superhéroe favorito.")
                            .porcentajeDescuento(14)
                            .fechaInicio(LocalDateTime.of(anioActual, 2, 10, 0, 0))
                            .fechaFin(LocalDateTime.of(anioActual, 2, 16, 23, 59, 59))
                            .activo(false)
                            .aplicaATodo(true)
                            .build(),
                    Promocion.builder()
                            .codigo("PROMO_PRUEBA_ACTIVA")
                            .nombre("Operación Relámpago S.H.I.E.L.D.")
                            .descripcion("Campaña promocional activa de prueba para verificación inmediata del checkout.")
                            .porcentajeDescuento(10)
                            .fechaInicio(ahora.minusDays(2))
                            .fechaFin(ahora.plusDays(5))
                            .activo(false)
                            .aplicaATodo(true)
                            .build()
            );

            promocionRepository.saveAll(semillas);
        }

        // Semillas iniciales de cupones de descuento canjeables
        if (cuponRepository.count() == 0) {
            List<CuponDescuento> cuponesIniciales = List.of(
                    CuponDescuento.builder()
                            .codigo("STARK10")
                            .porcentajeDescuento(10)
                            .activo(true)
                            .usosActuales(0)
                            .build(),
                    CuponDescuento.builder()
                            .codigo("AVENGERS15")
                            .porcentajeDescuento(15)
                            .activo(true)
                            .usosActuales(0)
                            .build(),
                    CuponDescuento.builder()
                            .codigo("HALLOWEEN")
                            .porcentajeDescuento(20)
                            .activo(true)
                            .usosActuales(0)
                            .build()
            );
            cuponRepository.saveAll(cuponesIniciales);
        }

        // Descuentos fijos en las figuras de Groot y Thor
        List<Producto> todosProductos = productoRepository.findAll();
        for (Producto prod : todosProductos) {
            if (prod.getPorcentajeDescuento() == null) {
                prod.setPorcentajeDescuento(0);
            }
            if (prod.getTieneDescuentoFijo() == null) {
                prod.setTieneDescuentoFijo(false);
            }

            String nombre = prod.getNombre() != null ? prod.getNombre().toLowerCase(Locale.ROOT) : "";
            String personaje = prod.getPersonaje() != null ? prod.getPersonaje().toLowerCase(Locale.ROOT) : "";
            boolean esFigura = nombre.contains("figura")
                    || (prod.getImagenUrl() != null && prod.getImagenUrl().toLowerCase(Locale.ROOT).contains("figuras"));

            boolean esGroot = nombre.contains("groot") || personaje.contains("groot");
            boolean esThor = (nombre.contains("thor") || personaje.contains("thor")) && esFigura;

            if ((esGroot || esThor) && (!Boolean.TRUE.equals(prod.getTieneDescuentoFijo()) || prod.getPorcentajeDescuento() <= 0)) {
                prod.setPorcentajeDescuento(15);
                prod.setTieneDescuentoFijo(true);
                productoRepository.save(prod);
            }
        }
    }
}
