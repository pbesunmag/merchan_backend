package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {

    // Comprueba si ya existe una categoría con ese nombre para evitar duplicados.
    boolean existsByNombreIgnoreCase(String nombre);
}