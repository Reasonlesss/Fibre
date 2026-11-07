package codes.reason.fibre.member;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class BoundField {

    private final Object owner;
    private final Class<?> type;
    private final Field field;
    private final Getter getter;
    private final @Nullable Setter setter;

    public BoundField(Object owner, Class<?> type, Field field, Getter getter, @Nullable Setter setter) {
        this.owner = Objects.requireNonNull(owner, "owner must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.field = Objects.requireNonNull(field, "field must not be null");
        this.getter = Objects.requireNonNull(getter, "getter must not be null");
        this.setter = setter;
    }

    public static Field lookup(Class<?> type, String name) {
        try {
            return type.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public @Nullable Object get() throws Throwable {
        return this.getter.get(this.owner);
    }

    public void set(@Nullable Object value) throws Throwable {
        if (this.setter == null) {
            throw new IllegalStateException("%s is not writable".formatted(this));
        }
        if (value == null && this.field.getType().isPrimitive()) {
            throw new IllegalArgumentException("%s is %s and cannot be null"
                    .formatted(this, this.field.getType().getSimpleName()));
        }
        if (value != null && !wrap(this.field.getType()).isInstance(value)) {
            throw new IllegalArgumentException("%s is %s, but got %s"
                    .formatted(
                            this,
                            this.field.getType().getSimpleName(),
                            value.getClass().getSimpleName()));
        }
        this.setter.set(this.owner, value);
    }

    public boolean isWritable() {
        return this.setter != null;
    }

    public FieldCheck signature() {
        return new FieldCheck(this);
    }

    public Object owner() {
        return this.owner;
    }

    public Class<?> type() {
        return this.type;
    }

    public Field field() {
        return this.field;
    }

    @Override
    public String toString() {
        return "%s#%s".formatted(this.type.getSimpleName(), this.field.getName());
    }

    private static Class<?> wrap(Class<?> type) {
        return MethodType.methodType(type).wrap().returnType();
    }
}
