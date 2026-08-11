package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.Producto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE " +
            "(:activo IS NULL OR p.activo = :activo) AND " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
    List<Producto> buscarConFiltros(@Param("activo") Boolean activo,
                                    @Param("categoriaId") Long categoriaId,
                                    Sort sort);

    boolean existsByNombreIgnoreCase(String nombre);
}