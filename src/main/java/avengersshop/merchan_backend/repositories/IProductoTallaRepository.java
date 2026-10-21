package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.ProductoTalla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductoTallaRepository extends JpaRepository<ProductoTalla, Long> {

    List<ProductoTalla> findByProductoId(Long productoId);

    Optional<ProductoTalla> findByProductoIdAndTallaIgnoreCase(Long productoId, String talla);
}
