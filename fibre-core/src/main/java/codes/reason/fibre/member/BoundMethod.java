package codes.reason.fibre.member;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class BoundMethod {

    private final Object owner;
    private final Class<?> type;
    private final Method method;
    private final Invoker invoker;

    public BoundMethod(Object owner, Class<?> type, Method method, Invoker invoker) {
        this.owner = Objects.requireNonNull(owner, "owner must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.method = Objects.requireNonNull(method, "method must not be null");
        this.invoker = Objects.requireNonNull(invoker, "invoker must not be null");
    }

    public static Method lookup(Class<?> type, String name, Class<?>... parameters) {
        try {
            return type.getDeclaredMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    public @Nullable Object invoke(@Nullable Object... args) throws Throwable {
        Objects.requireNonNull(args, "args must not be null");
        if (args.length != this.method.getParameterCount()) {
            throw new IllegalArgumentException(
                    "%s takes %d parameters, but got %d".formatted(this, this.method.getParameterCount(), args.length));
        }
        return this.invoker.invoke(this.owner, args);
    }

    public MethodCheck signature() {
        return new MethodCheck(this);
    }

    public Object owner() {
        return this.owner;
    }

    public Class<?> type() {
        return this.type;
    }

    public Method method() {
        return this.method;
    }

    @Override
    public String toString() {
        String parameters = Arrays.stream(this.method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        return "%s#%s(%s)".formatted(this.type.getSimpleName(), this.method.getName(), parameters);
    }
}
