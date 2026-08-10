package cloud.emilys.fibre.core.type;

import cloud.emilys.fibre.api.type.TypeFinder;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record RegisteredTypeFinder<S, T>(Class<S> sourceType, Class<T> targetType, TypeFinder<S, T> finder) {

    public RegisteredTypeFinder {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(targetType, "targetType");
        Objects.requireNonNull(finder, "finder");
    }

    Optional<?> find(Object source) {
        return Objects.requireNonNull(this.finder.find(this.sourceType.cast(source)), "Type finder returned null");
    }
}
