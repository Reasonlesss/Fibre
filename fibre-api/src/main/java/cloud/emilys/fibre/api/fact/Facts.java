package cloud.emilys.fibre.api.fact;

import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Facts {

    <T> Optional<T> get(FactKey<T> key);

    default <T> boolean has(FactKey<T> key) {
        return this.get(key).isPresent();
    }

    default <T> T require(FactKey<T> key) {
        return this.get(key)
                .orElseThrow(() -> new IllegalStateException("Missing class fact %s".formatted(key.name())));
    }
}
