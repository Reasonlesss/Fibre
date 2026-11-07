package codes.reason.fibre.identity;

import java.lang.invoke.MethodType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class IdentityHelper {

    private IdentityHelper() {}

    static @Nullable String notBlank(@Nullable String name) {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return name;
    }

    static Type concrete(Type type) {
        if (type instanceof TypeVariable<?> || type instanceof WildcardType) {
            throw new IllegalArgumentException("type must be concrete, got " + type.getTypeName());
        }
        return type;
    }

    static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return MethodType.methodType(type).wrap().returnType();
    }

    static Class<?> erase(Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterized -> erase(parameterized.getRawType());
            case GenericArrayType array ->
                erase(array.getGenericComponentType()).arrayType();
            default -> throw new IllegalArgumentException("cannot erase " + type.getTypeName());
        };
    }

    static boolean equals(Identity<?> identity, @Nullable Object o) {
        return o instanceof Identity<?> other
                && identity.type().equals(other.type())
                && Objects.equals(identity.name(), other.name());
    }

    static int hashCode(Identity<?> identity) {
        return Objects.hash(identity.type(), identity.name());
    }

    static String toString(Identity<?> identity) {
        String name = identity.name();
        return name != null
                ? "@Named(\"%s\") %s".formatted(name, identity.type().getTypeName())
                : identity.type().getTypeName();
    }
}
