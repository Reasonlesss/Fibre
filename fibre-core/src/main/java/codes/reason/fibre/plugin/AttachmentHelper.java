package codes.reason.fibre.plugin;

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
final class AttachmentHelper {

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private AttachmentHelper() {}

    // the first frame outside the attachment classes, so token subclasses resolve to where they were declared
    static Class<?> caller() {
        return WALKER.walk(frames -> frames
                .map(StackWalker.StackFrame::getDeclaringClass)
                .filter(type -> type != AttachmentHelper.class && !Attachment.class.isAssignableFrom(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Attachment must be created by a caller")));
    }

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

    static boolean equals(Attachment<?> attachment, @Nullable Object o) {
        return o instanceof Attachment<?> other
                && attachment.owner().equals(other.owner())
                && attachment.type().equals(other.type())
                && Objects.equals(attachment.name(), other.name());
    }

    static int hashCode(Attachment<?> attachment) {
        return Objects.hash(attachment.owner(), attachment.type(), attachment.name());
    }

    static String toString(Attachment<?> attachment) {
        String name = attachment.name();
        String type = name != null
                ? "%s \"%s\"".formatted(attachment.type().getTypeName(), name)
                : attachment.type().getTypeName();
        return "%s:%s".formatted(attachment.owner().getName(), type);
    }
}
