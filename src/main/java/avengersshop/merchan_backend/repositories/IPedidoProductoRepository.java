package avengersshop.merchan_backend.repositories;

import avengersshop.merchan_backend.dto.response.ProductoMasVendidoDTO;
import avengersshop.merchan_backend.models.PedidoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPedidoProductoRepository extends JpaRepository<PedidoProducto, Long> {

    // Ranking de productos por unidades vendidas (excluye pedidos cancelados), del más al menos vendido.
    @Query("SELECT new avengersshop.merchan_backend.dto.response.ProductoMasVendidoDTO(" +
           "pp.producto.id, pp.producto.nombre, SUM(pp.cantidad)) " +
           "FROM PedidoProducto pp " +
           "WHERE pp.pedido.estado NOT IN (avengersshop.merchan_backend.models.EstadoPedido.CANCELADO, " +
           "avengersshop.merchan_backend.models.EstadoPedido.DEVUELTO) " +
           "GROUP BY pp.producto.id, pp.producto.nombre " +
           "ORDER BY SUM(pp.cantidad) DESC")
    List<ProductoMasVendidoDTO> findProductosMasVendidos();
}
