package codes.reason.fibre;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public final class Attachment<T> {

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private final Class<?> caller;
    private final String name;
    private final Class<?> type;

    private Attachment(Class<?> caller, String name, Class<?> type) {
        this.caller = caller;
        this.name = name;
        this.type = type;
    }

    public static <T> Attachment<T> of(Class<? super T> type, String name) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Primitive types are not supported, use a wrapper type instead");
        }
        return new Attachment<>(WALKER.getCallerClass(), name, type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public T cast(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (!this.type.isInstance(value)) {
            throw new ClassCastException("%s represents %s, not %s"
                    .formatted(this, this.type.getName(), value.getClass().getName()));
        }
        return (T) this.type.cast(value);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof Attachment<?> that)) {
            return false;
        }
        return Objects.equals(caller, that.caller)
                && Objects.equals(name, that.name)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caller, name, type);
    }

    @Override
    public String toString() {
        return "Attachment<" + this.caller.getName() + ":" + this.name + ">";
    }

}
