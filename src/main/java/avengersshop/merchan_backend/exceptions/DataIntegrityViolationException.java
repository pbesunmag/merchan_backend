package avengersshop.merchan_backend.exceptions;

// Excepción personalizada para representar errores de integridad en la base de datos.
// Se lanza cuando una operación viola una restricción del modelo (ej: claves duplicadas o relaciones faltantes).
public class DataIntegrityViolationException extends RuntimeException {
    public DataIntegrityViolationException(String message) {
        super(message);
    }
}
