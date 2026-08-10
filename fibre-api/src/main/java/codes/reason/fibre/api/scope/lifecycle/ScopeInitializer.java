package codes.reason.fibre.api.scope.lifecycle;

import codes.reason.fibre.api.scope.Scope;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface ScopeInitializer {

    void initialize(Scope scope);
}
