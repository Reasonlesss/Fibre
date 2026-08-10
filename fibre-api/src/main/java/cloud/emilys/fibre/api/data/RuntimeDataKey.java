package cloud.emilys.fibre.api.data;

import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record RuntimeDataKey<T>(String namespace, String name, Class<?> valueType) {

    public static <T> RuntimeDataKey<T> of(String namespace, String name, Class<?> valueType) {
        return new RuntimeDataKey<>(namespace, name, valueType);
    }

    public RuntimeDataKey {
        namespace = Objects.requireNonNull(namespace, "namespace");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("Runtime data key namespace must not be blank");
        }
        name = Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Runtime data key name must not be blank");
        }
        Objects.requireNonNull(valueType, "valueType");
    }

    @SuppressWarnings("unchecked")
    public @Nullable T cast(@Nullable Object value) {
        if (value != null && !this.valueType.isInstance(value)) {
            throw new ClassCastException("Runtime data key '%s:%s' expected %s but received %s"
                    .formatted(
                            this.namespace,
                            this.name,
                            this.valueType.getName(),
                            value.getClass().getName()));
        }
        return (T) value;
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.name + "[" + this.valueType.getName() + "]";
    }
}
