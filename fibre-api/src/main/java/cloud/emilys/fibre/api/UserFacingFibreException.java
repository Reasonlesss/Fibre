package cloud.emilys.fibre.api;

public abstract class UserFacingFibreException extends RuntimeException {

    public UserFacingFibreException(String message) {
        super(message);
    }
}
