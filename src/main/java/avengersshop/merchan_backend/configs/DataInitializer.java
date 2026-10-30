package avengersshop.merchan_backend.configs;

import avengersshop.merchan_backend.models.MovimientoCaja;
import avengersshop.merchan_backend.models.TipoMovimientoCaja;
import avengersshop.merchan_backend.repositories.IMovimientoCajaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Si la caja está vacía al arrancar, registra el fondo inicial.
@Component
public class DataInitializer implements CommandLineRunner {

    private final IMovimientoCajaRepository movimientoCajaRepository;

    public DataInitializer(IMovimientoCajaRepository movimientoCajaRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
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
    }
}
