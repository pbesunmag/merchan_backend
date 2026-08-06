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

 @Column(name = "descripcion")
    private String descripcion;

 @Column(name = "precio")
    private BigDecimal precio;

 @Column(name = "disponible")
    private Boolean disponible;

}
