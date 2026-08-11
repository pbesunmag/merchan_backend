package avengersshop.merchan_backend.services;

import avengersshop.merchan_backend.dto.response.PedidoDTO;
import avengersshop.merchan_backend.dto.response.PedidoPantallaDTO;
import avengersshop.merchan_backend.exceptions.ResourceNotFoundException;
import avengersshop.merchan_backend.models.EstadoPedido;
import avengersshop.merchan_backend.models.Pedido;
import avengersshop.merchan_backend.repositories.IPedidoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PedidoService {

    private final IPedidoRepository iPedidoRepository;

    public PedidoService(IPedidoRepository iPedidoRepository) {
        this.iPedidoRepository = iPedidoRepository;
    }

    // Listar pedidos de forma paginada y con filtro de estado opcional
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPedidos(EstadoPedido estado, Pageable pageable) {
        Page<Pedido> pedidos = iPedidoRepository.buscarConFiltroEstado(estado, pageable);
        return pedidos.map(PedidoDTO::fromEntity);
    }

    // Buscar pedido por código único
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPorCodigo(String codigo) {
        Pedido pedido = iPedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con código: " + codigo));
        return PedidoDTO.fromEntity(pedido);
    }

    // Listar pedidos simplificados para la pantalla del local
    @Transactional(readOnly = true)
    public List<PedidoPantallaDTO> listarPedidosPantalla() {
        List<Pedido> pedidos = iPedidoRepository.findAllByOrderByFechaCreacionAsc();
        return pedidos.stream()
                .map(PedidoPantallaDTO::fromEntity)
                .toList();
    }
}