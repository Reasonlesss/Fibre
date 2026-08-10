package cloud.emilys.fibre.api.type;

import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TypeResolver {

    <T> Optional<T> find(Object source, Class<T> targetType);
}
