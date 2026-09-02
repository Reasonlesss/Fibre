package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.lifecycle.ScopeInitializer;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadInitializer implements ScopeInitializer {

    @Override
    public void initialize(Scope scope) {
        Optional<List<Method>> optional = scope.get(PlayerPreloadDataKeys.METHODS);
        if (optional.isEmpty()) {
            return;
        }
        scope.put(PlayerPreloadDataKeys.METHODS, List.copyOf(optional.get()));
    }
}
