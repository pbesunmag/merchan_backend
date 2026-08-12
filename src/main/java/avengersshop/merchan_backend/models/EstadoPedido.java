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
}