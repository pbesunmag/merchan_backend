package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.MovimientoCaja;
import avengersshop.merchan_backend.models.TipoMovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IMovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    // Último movimiento (sin bloqueo: MySQL/MariaDB no admite "FOR UPDATE OF <alias>").
    Optional<MovimientoCaja> findFirstByOrderByIdDesc();

    // Último movimiento sin bloqueo (consultas de solo lectura).
    Optional<MovimientoCaja> findTopByOrderByIdDesc();

    // Movimientos de un intervalo de tiempo.
    List<MovimientoCaja> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);

    // Último movimiento anterior a una fecha (saldo de apertura de un periodo).
    Optional<MovimientoCaja> findTopByFechaBeforeOrderByIdDesc(LocalDateTime fecha);

    // Comprueba si ya existe un tipo de movimiento para un pedido (evita duplicar ventas o reembolsos).
    boolean existsByPedidoIdAndTipoMovimiento(Long pedidoId, TipoMovimientoCaja tipoMovimiento);
}
