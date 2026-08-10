package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.models.PedidoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPedidoProductoRepository extends JpaRepository<PedidoProducto, Long> {


}
