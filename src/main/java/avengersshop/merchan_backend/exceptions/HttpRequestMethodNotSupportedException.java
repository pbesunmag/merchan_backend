package avengersshop.merchan_backend.exceptions;

public class HttpRequestMethodNotSupportedException extends RuntimeException {
    public HttpRequestMethodNotSupportedException(String message) {
        super(message);
    }
}
