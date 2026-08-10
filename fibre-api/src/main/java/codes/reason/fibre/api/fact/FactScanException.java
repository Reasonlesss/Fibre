package codes.reason.fibre.api.fact;

import codes.reason.fibre.api.UserFacingFibreException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FactScanException extends UserFacingFibreException {

    public FactScanException(String message) {
        super(message);
    }
}
