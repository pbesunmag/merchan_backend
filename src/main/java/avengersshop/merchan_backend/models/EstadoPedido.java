package avengersshop.merchan_backend.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

// Estados por los que pasa una compra desde su creación hasta la entrega al cliente.
public enum EstadoPedido {
    CREADO("CREADO"),
    CONFIRMACION_DE_PAGO("CONFIRMACION DE PAGO"),
    PAGO_ACEPTADO("PAGO ACEPTADO"),
    EN_PREPARACION("EN PREPARACION"),
    PEDIDO_LISTO("PEDIDO LISTO"),
    ENVIADO("ENVIADO"),
    ENTREGADO("ENTREGADO"),
    CANCELADO("CANCELADO");

    private final String value;

    EstadoPedido(String value) {
        this.value = value;
    }

    // Devuelve el texto legible del estado en las respuestas JSON de la API.
    @JsonValue
    public String getValue() {
        return value;
    }

    // Permite a Postman/Frontend enviar tanto "CONFIRMACION DE PAGO" como "CONFIRMACION_DE_PAGO", sin importar mayúsculas o minúsculas
    @JsonCreator
    public static EstadoPedido fromValue(String value) {
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (estado.value.equalsIgnoreCase(value) || estado.name().equalsIgnoreCase(value)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido no válido: " + value);
    }

    // Valida si la transición desde el estado actual hacia el nuevo estado es lícita.
    public boolean puedeTransicionarA(EstadoPedido nuevoEstado) {
        if (nuevoEstado == null || this == nuevoEstado) {
            return false;
        }

        // Estados finales irreversibles
        if (this == ENTREGADO || this == CANCELADO) {
            return false;
        }

        // La cancelación está permitida desde cualquier estado activo previo a la entrega
        if (nuevoEstado == CANCELADO) {
            return true;
        }

        return switch (this) {
            case CREADO -> nuevoEstado == CONFIRMACION_DE_PAGO
                        || nuevoEstado == PAGO_ACEPTADO
                        || nuevoEstado == EN_PREPARACION;

            case CONFIRMACION_DE_PAGO -> nuevoEstado == PAGO_ACEPTADO
                                      || nuevoEstado == EN_PREPARACION;

            case PAGO_ACEPTADO -> nuevoEstado == EN_PREPARACION;

            case EN_PREPARACION -> nuevoEstado == PEDIDO_LISTO
                                || nuevoEstado == CREADO; // Permite revertir desde cocina

            case PEDIDO_LISTO -> nuevoEstado == ENVIADO
                              || nuevoEstado == ENTREGADO
                              || nuevoEstado == EN_PREPARACION; // Permite revertir desde mostrador

            case ENVIADO -> nuevoEstado == ENTREGADO;

            default -> false;
        };
    }
}