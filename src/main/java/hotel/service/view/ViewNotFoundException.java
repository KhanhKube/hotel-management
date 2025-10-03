package hotel.service.view;

public class ViewNotFoundException extends RuntimeException {
    public ViewNotFoundException(String message) {
        super(message);
    }
}
