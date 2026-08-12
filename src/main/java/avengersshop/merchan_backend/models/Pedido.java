package avengersshop.merchan_backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@EntityListeners(AuditingEntityListener.class) // Activa la auditoría automática para campos de fecha
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY para optimizar rendimiento evitando consultas innecesarias a la tabla de terminales.
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    // Líneas de detalle con los productos comprados en este pedido.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProducto> productos = new ArrayList<>();

    // Marca de tiempo de cuando se registró la compra.
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(nullable = false) // Se actualiza automáticamente en cada modificación del registro
    private LocalDateTime fechaActualizacion;
}