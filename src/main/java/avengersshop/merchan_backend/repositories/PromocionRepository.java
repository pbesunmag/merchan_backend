package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    @Query("SELECT p FROM Promocion p WHERE p.activo = true AND :ahora BETWEEN p.fechaInicio AND p.fechaFin ORDER BY p.porcentajeDescuento DESC")
    List<Promocion> findPromocionesVigentes(@Param("ahora") LocalDateTime ahora);

    Optional<Promocion> findFirstByActivoTrueAndFechaInicioBeforeAndFechaFinAfterOrderByPorcentajeDescuentoDesc(
            LocalDateTime inicio, LocalDateTime fin);

    Optional<Promocion> findByCodigo(String codigo);

    List<Promocion> findByActivoTrueOrderByPorcentajeDescuentoDesc();
}
