package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

    // Buscamos un pedido específico por su código único
    Optional<Pedido> findByCodigo(String codigo);

    // Listamos todos los pedidios por fecha ascendente
    List<Pedido> findAllByOrderByFechaCreacionAsc();

    // Listamos pedidos filtrados por estado y ordenados por fecha ascendente
    List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);

}
