package avengersshop.merchan_backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class) // Activa la auditoría automática de fechas manejada por Spring Data JPA
@Table(name = "productos")
// Se utilizan @Getter y @Setter en lugar de @Data para evitar consultas no deseadas y ciclos infinitos en relaciones JPA
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(length = 1000, name = "descripcion") // Amplía el límite predeterminado a 1000 caracteres para descripciones detalladas
    private String descripcion;

    private String personaje;

    // Mantiene precisión monetaria exacta sin errores de redondeo
    @Column(nullable = false, precision = 10, scale = 2 , name = "precio")
    private BigDecimal precio;

    // Indica si el producto permite grabado o estampado personalizado por el cliente
    @Column(nullable = false, name = "personalizable")
    private boolean personalizable = false;

    // Permite desactivar productos descatalogados sin romper la integridad referencial de los pedidos históricos donde este producto fue vendido.
    @Column(nullable = false, name = "activo")
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY para optimizar el rendimiento y evitar consultas redundantes a la tabla de categorías.
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Auditoría automática de fechas gestionada por Spring Data JPA
    @CreatedDate
    @Column(nullable = false, updatable = false, name = "fecha_creacion") // La fecha de creación no se puede modificar tras registrarse
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(nullable = false, name = "fecha_actualizacion") // Se actualiza automáticamente en cada modificación del registro
    private LocalDateTime fechaActualizacion;
}
