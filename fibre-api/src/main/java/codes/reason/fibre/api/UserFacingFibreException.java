package codes.reason.fibre.api;

public abstract class UserFacingFibreException extends RuntimeException {

    public UserFacingFibreException(String message) {
        super(message);
    }
}
