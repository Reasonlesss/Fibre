package cloud.emilys.fibre.api.fact;

import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record FactKey<T>(String namespace, String name, Class<?> valueType) {

    public static <T> FactKey<T> of(String namespace, String name, Class<?> valueType) {
        return new FactKey<>(namespace, name, valueType);
    }

    public FactKey {
        namespace = Objects.requireNonNull(namespace, "namespace");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("Fact key namespace must not be blank");
        }
        name = Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Fact key name must not be blank");
        }
        Objects.requireNonNull(valueType, "valueType");
    }

    @SuppressWarnings("unchecked")
    public @Nullable T cast(@Nullable Object value) {
        if (value != null && !this.valueType.isInstance(value)) {
            throw new ClassCastException("Fact key '%s:%s' expected %s but received %s"
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
