package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

    // Busca un pedido por su código único de seguimiento.
    Optional<Pedido> findByCodigo(String codigo);

    // Consulta unificada con filtro opcional por estado y soporte de paginación/ordenación
    @Query("SELECT p FROM Pedido p WHERE :estado IS NULL OR p.estado = :estado")
    Page<Pedido> buscarConFiltroEstado(@Param("estado") EstadoPedido estado, Pageable pageable);

    // --- Métodos sin paginar (útiles para pantallas o listados fijos) ---

    // Obtiene los pedidos ordenados desde el más antiguo
    List<Pedido> findAllByOrderByFechaCreacionAsc();

    // Lista los pedidos de un estado en particular ordenados por antigüedad.
    List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);
}