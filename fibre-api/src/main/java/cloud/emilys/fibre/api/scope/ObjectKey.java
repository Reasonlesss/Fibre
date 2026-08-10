package cloud.emilys.fibre.api.scope;

import cloud.emilys.fibre.api.annotation.dependency.Named;
import java.lang.reflect.*;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ObjectKey {

    private final Type type;
    private final @Nullable String qualifier;
    private final @Nullable AnnotatedType annotatedType;

    public ObjectKey(Type type, @Nullable String qualifier) {
        this(type, qualifier, null);
    }

    private ObjectKey(Type type, @Nullable String qualifier, @Nullable AnnotatedType annotatedType) {
        this.type = Objects.requireNonNull(type, "type");
        if (qualifier != null && qualifier.isBlank()) {
            throw new IllegalArgumentException("Qualifier must not be blank");
        }
        this.qualifier = qualifier;
        this.annotatedType = annotatedType;
    }

    public static ObjectKey fromType(Type type) {
        return new ObjectKey(type, null);
    }

    public static ObjectKey fromNamedType(Type type, @Nullable String qualifier) {
        return new ObjectKey(type, qualifier);
    }

    public static ObjectKey fromAnnotatedType(AnnotatedType type) {
        Named named = type.getAnnotation(Named.class);
        return new ObjectKey(type.getType(), named == null ? null : named.value(), type);
    }

    public Type type() {
        return this.type;
    }

    public @Nullable String qualifier() {
        return this.qualifier;
    }

    public Optional<AnnotatedType> getAnnotatedType() {
        return Optional.ofNullable(this.annotatedType);
    }

    public Class<?> getObjectClass() {
        return getClassOfType(this.type);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ObjectKey other)) {
            return false;
        }
        return this.type.equals(other.type) && Objects.equals(this.qualifier, other.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.qualifier);
    }

    @Override
    public String toString() {
        return "ObjectKey[type=" + this.type + ", qualifier=" + this.qualifier + "]";
    }

    private static Class<?> getClassOfType(Type type) {
        return switch (type) {
            case Class<?> aClass -> aClass;
            case ParameterizedType parameterizedType -> getClassOfType(parameterizedType.getRawType());
            case GenericArrayType genericArrayType ->
                Array.newInstance(getClassOfType(genericArrayType.getGenericComponentType()), 0)
                        .getClass();
            case TypeVariable<?> typeVariable -> getClassOfType(typeVariable.getBounds()[0]);
            case WildcardType wildcardType -> getClassOfType(wildcardType.getUpperBounds()[0]);
            default -> throw new IllegalArgumentException("Unsupported type: %s".formatted(type));
        };
    }
}
