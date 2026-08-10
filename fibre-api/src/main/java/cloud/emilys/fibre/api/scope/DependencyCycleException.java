package cloud.emilys.fibre.api.scope;

import cloud.emilys.fibre.api.UserFacingFibreException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencyCycleException extends UserFacingFibreException {
    public DependencyCycleException(String message) {
        super(message);
    }
}
