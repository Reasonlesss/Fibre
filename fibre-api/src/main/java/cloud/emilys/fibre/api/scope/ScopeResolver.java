package cloud.emilys.fibre.api.scope;

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopeResolver {

    Optional<ScopedObject> get(ObjectKey key);

    default ScopedObject require(ObjectKey key) {
        Objects.requireNonNull(key, "key");
        return this.get(key).orElseThrow(() -> new IllegalStateException("No object is bound for %s".formatted(key)));
    }
}
