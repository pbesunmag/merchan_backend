package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.CuponDescuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuponDescuentoRepository extends JpaRepository<CuponDescuento, Long> {

    Optional<CuponDescuento> findByCodigoIgnoreCase(String codigo);

    List<CuponDescuento> findByActivoTrueOrderByIdAsc();
}
