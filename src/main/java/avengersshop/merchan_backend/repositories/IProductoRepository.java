package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.Producto;
import org.springframework.data.domain.Sort; // 👈 Ojo a esta importación
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivo(boolean activo, Sort sort);

    List<Producto> findByCategoriaId(Long categoriaId, Sort sort);

    List<Producto> findByActivoAndCategoriaId(boolean activo, Long categoriaId, Sort sort);
}