package avengersshop.merchan_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Excepción personalizada para representar recursos no encontrados (HTTP 404 Not Found).
// Mapea automáticamente la respuesta a código 404 cuando la entidad solicitada no existe.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
