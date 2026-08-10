package avengersshop.merchan_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo; // Ej: "AV-1001"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.CREADO;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProducto> lineas = new ArrayList<>();

    // Métodos helper para mantener ambos lados de la relación sincronizados
    public void agregarLinea(PedidoProducto linea) {
        lineas.add(linea);
        linea.setPedido(this);
    }

    public void removerLinea(PedidoProducto linea) {
        lineas.remove(linea);
        linea.setPedido(null);
    }
}