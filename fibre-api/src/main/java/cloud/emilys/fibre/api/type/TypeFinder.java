package cloud.emilys.fibre.api.type;

import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface TypeFinder<S, T> {

    Optional<T> find(S source);
}
