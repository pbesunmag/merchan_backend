package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.response.ProductoMasVendidoDTO;
import avengersshop.merchan_backend.repositories.IPedidoProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // Todas las operaciones son de solo lectura
public class EstadisticaService {

    private final IPedidoProductoRepository pedidoProductoRepository;

    public EstadisticaService(IPedidoProductoRepository pedidoProductoRepository) {
        this.pedidoProductoRepository = pedidoProductoRepository;
    }

    // Devuelve el ranking completo de productos vendidos (sin contar pedidos cancelados).
    public List<ProductoMasVendidoDTO> listarProductosMasVendidos() {
        return pedidoProductoRepository.findProductosMasVendidos();
    }

    // Devuelve el producto más vendido, o vacío si aún no hay ventas.
    public Optional<ProductoMasVendidoDTO> obtenerProductoMasVendido() {
        return pedidoProductoRepository.findProductosMasVendidos().stream().findFirst();
    }
}
