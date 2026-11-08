package codes.reason.fibre.scope;

import codes.reason.fibre.identity.Identity;
import codes.reason.fibre.identity.TokenIdentity;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Handle<T> {

    public static final Identity<Handle<Scope>> SCOPE = new TokenIdentity<>() {};

    // null for a scope's own handle, which is set once the scope is built
    private final @Nullable Supplier<? extends @Nullable T> resolver;
    private volatile @Nullable T value;

    private Handle(@Nullable Supplier<? extends @Nullable T> resolver) {
        this.resolver = resolver;
    }

    static Handle<Scope> scope() {
        return new Handle<>(null);
    }

    public <R> Handle<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new Handle<>(() -> {
            T value = this.resolve();
            return value != null ? Objects.requireNonNull(mapper.apply(value), "mapper must not return null") : null;
        });
    }

    public Optional<T> get() {
        return Optional.ofNullable(this.resolve());
    }

    public T require() {
        T value = this.resolve();
        if (value == null) {
            throw new IllegalStateException("Scope hasn't been built yet");
        }
        return value;
    }

    public boolean isResolved() {
        return this.resolve() != null;
    }

    void set(T value) {
        this.value = value;
    }

    private @Nullable T resolve() {
        T value = this.value;
        if (value == null && this.resolver != null) {
            value = this.resolver.get();
            if (value != null) {
                this.value = value;
            }
        }
        return value;
    }
}
