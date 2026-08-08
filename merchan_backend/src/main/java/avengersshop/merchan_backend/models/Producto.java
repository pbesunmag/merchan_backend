package avengersshop.merchan_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productos")

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(length = 1000, name = "descripcion")
    private String descripcion;

    private String personaje;

    @Column(nullable = false, precision = 10, scale = 2 , name = "precio")
    private BigDecimal precio;

    @Column(nullable = false, name = "disponible")
    private Boolean disponible;

    @Column(nullable = false, name = "personalizable")
    private boolean personalizable = false;

    @Column(nullable = false, name = "activo")
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;


}
