package cloud.emilys.fibre.api.scope.lifecycle;

import cloud.emilys.fibre.api.scope.Scope;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface ScopeInitializer {

    void initialize(Scope scope);
}
