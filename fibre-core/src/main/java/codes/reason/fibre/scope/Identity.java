package codes.reason.fibre.scope;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;

@NullMarked
public abstract class Identity<T> {

    private final Type type;
    private final Class<? super T> rawType;
    private final @Nullable String name;

    /**
     * Captures {@code T} from the anonymous subclass's type argument.
     */
    protected Identity() {
        this(null);
    }

    /**
     * Captures {@code T} from the anonymous subclass's type argument, qualified by the given name.
     */
    @SuppressWarnings("unchecked")
    protected Identity(@Nullable String name) {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Type superclass = this.getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterized) || parameterized.getRawType() != Identity.class) {
            throw new IllegalStateException("Identity must be created as a direct anonymous subclass, e.g. new Identity<List<String>>() {}");
        }
        this.type = requireConcrete(parameterized.getActualTypeArguments()[0]);
        this.rawType = (Class<? super T>) erase(this.type);
        this.name = name;
    }

    public final Type type() {
        return this.type;
    }

    public final Class<? super T> rawType() {
        return this.rawType;
    }

    public final @Nullable String name() {
        return this.name;
    }

    /**
     * Casts a value to {@code T}. Only the raw type is checked at runtime, so type arguments are not verified.
     */
    @SuppressWarnings("unchecked")
    public final T cast(Object value) {
        Class<?> boxed = box(this.rawType);
        return (T) boxed.cast(value);
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof Identity<?> other
                && this.type.equals(other.type)
                && Objects.equals(this.name, other.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.type, this.name);
    }

    @Override
    public final String toString() {
        return this.name != null
                ? "@Named(\"%s\") %s".formatted(this.name, this.type.getTypeName())
                : this.type.getTypeName();
    }

    private static Type requireConcrete(Type type) {
        if (type instanceof TypeVariable<?> || type instanceof WildcardType) {
            throw new IllegalArgumentException("type must be concrete, got " + type.getTypeName());
        }
        return type;
    }

    private static Class<?> erase(Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterized -> erase(parameterized.getRawType());
            case GenericArrayType array -> erase(array.getGenericComponentType()).arrayType();
            default -> throw new IllegalArgumentException("cannot erase " + type.getTypeName());
        };
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return java.lang.invoke.MethodType.methodType(type).wrap().returnType();
    }

}
