package cloud.emilys.fibre.core.data;

import cloud.emilys.fibre.api.data.RuntimeData;
import cloud.emilys.fibre.api.data.RuntimeDataKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class RuntimeDataImpl implements RuntimeData {

    private final Map<RuntimeDataKey<?>, Object> data = new HashMap<>();

    @Override
    public <T> Optional<T> get(RuntimeDataKey<T> key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(key.cast(this.data.get(key)));
    }

    @Override
    public <T> void put(RuntimeDataKey<T> key, T value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        this.data.put(key, value);
    }
}
