package cloud.emilys.fibre.api.data;

import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface RuntimeData {

    <T> Optional<T> get(RuntimeDataKey<T> key);

    <T> void put(RuntimeDataKey<T> key, T value);
}
