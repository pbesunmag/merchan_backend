package avengersshop.merchan_backend.exceptions;

// Excepción personalizada para representar conflictos por recursos duplicados.
// Se lanza cuando se intenta registrar una entidad que ya existe previamente en el sistema.
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
