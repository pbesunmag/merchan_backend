package avengersshop.merchan_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "producto_tallas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_producto_talla", columnNames = {"producto_id", "talla"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoTalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "talla", length = 10, nullable = false)
    private String talla;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;
}
