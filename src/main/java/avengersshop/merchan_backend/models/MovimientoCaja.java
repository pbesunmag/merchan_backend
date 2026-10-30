package avengersshop.merchan_backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Apunte contable de la caja. Cada movimiento guarda el saldo resultante tras aplicarse.
@Entity
@Table(name = "movimientos_caja")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimientoCaja tipoMovimiento;

    // Positivo para ingresos y negativo para salidas (devoluciones)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoResultante;

    @Column(length = 500)
    private String descripcion;

    // Pedido asociado (nulo en el fondo inicial)
    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
