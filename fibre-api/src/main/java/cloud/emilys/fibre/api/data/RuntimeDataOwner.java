package cloud.emilys.fibre.api.data;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface RuntimeDataOwner {

    RuntimeData getRuntimeData();

    default <T> Optional<T> get(RuntimeDataKey<T> key) {
        return this.getRuntimeData().get(key);
    }

    default <T> T getOrPut(RuntimeDataKey<T> key, Supplier<T> supplier) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(supplier, "supplier");
        return this.get(key).orElseGet(() -> {
            T value = supplier.get();
            this.put(key, value);
            return value;
        });
    }

    default <T> void put(RuntimeDataKey<T> key, T value) {
        this.getRuntimeData().put(key, value);
    }
}
