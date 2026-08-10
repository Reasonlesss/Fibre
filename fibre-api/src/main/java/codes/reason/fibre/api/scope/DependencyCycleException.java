package codes.reason.fibre.api.scope;

import codes.reason.fibre.api.UserFacingFibreException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencyCycleException extends UserFacingFibreException {
    public DependencyCycleException(String message) {
        super(message);
    }
}
