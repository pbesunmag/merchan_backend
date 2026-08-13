package avengersshop.merchan_backend.exceptions;

// Excepción personalizada para representar errores de petición incorrecta (HTTP 400 Bad Request).
// Se lanza cuando el cliente envía datos inválidos o infringe una regla de negocio.
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
