package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Long> {

    // Filtra el catálogo de la tienda por estado (activo/inactivo) y categoría, mostrando los resultados por páginas.
    @Query("SELECT p FROM Producto p WHERE " +
            "(:activo IS NULL OR p.activo = :activo) AND " +
            "(:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
    Page<Producto> buscarConFiltros(@Param("activo") Boolean activo,
                                    @Param("categoriaId") Long categoriaId,
                                    Pageable pageable);

    // Verifica si ya existe un merchandising registrado con ese nombre para evitar crear productos repetidos.
    boolean existsByNombreIgnoreCase(String nombre);

    // Recupera el producto completo por nombre para acceder a su ID en caso de conflicto
    Optional<Producto> findByNombreIgnoreCase(String nombre);
}