package avengersshop.merchan_backend.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    public String getValue() {
        return value;
    }

    // Permite a Postman/Frontend enviar tanto "CONFIRMACION DE PAGO" como "CONFIRMACION_DE_PAGO"
    @JsonCreator
    public static EstadoPedido fromValue(String value) {
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (estado.value.equalsIgnoreCase(value) || estado.name().equalsIgnoreCase(value)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido no válido: " + value);
    }
}