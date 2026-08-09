package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITerminalRepository extends JpaRepository<Terminal, Long> {

    // Comprobación de duplicados ignorando mayúsculas y minúsculas
    boolean existsByNombreIgnoreCase(String nombre);
}