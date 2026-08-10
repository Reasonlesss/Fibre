package cloud.emilys.fibre.api.fact;

import cloud.emilys.fibre.api.UserFacingFibreException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FactScanException extends UserFacingFibreException {

    public FactScanException(String message) {
        super(message);
    }
}
