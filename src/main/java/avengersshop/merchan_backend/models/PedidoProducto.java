package avengersshop.merchan_backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Table(name = "pedido_producto")
//Con @Getter y @Setter en lugar de poner @Data evitamos carga no deseada y ciclos infinitos
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY para optimizar memoria al consultar líneas individuales sin cargar todo el pedido.
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY para evitar llamadas innecesarias al catálogo de productos.
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad; // Número de unidades solicitadas de este producto en particular

    // Guardado del precio histórico unitario.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Campo de texto libre para grabados o estampados personalizados (ej. nombre para la camiseta de Marvel)
    @Column(length = 500)
    private String textoPersonalizado;
}