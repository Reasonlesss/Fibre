package codes.reason.fibre.game;

import codes.reason.fibre.annotations.Game;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.ScopeOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GameBuilder {

    private final List<Consumer<? super ScopeOptions>> scopeOptions = new ArrayList<>();
    private final List<ResultListener<?>> listeners = new ArrayList<>();
    private @Nullable Class<?> type;
    private boolean built;

    private GameBuilder() {}

    public static GameBuilder create() {
        return new GameBuilder();
    }

    public GameBuilder type(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        this.ensureNotBuilt();
        if (!type.isAnnotationPresent(Game.class)) {
            throw new IllegalArgumentException("%s is not annotated with @Game".formatted(type.getName()));
        }
        this.type = type;
        return this;
    }

    public GameBuilder scope(Consumer<? super ScopeOptions> options) {
        Objects.requireNonNull(options, "options must not be null");
        this.ensureNotBuilt();
        this.scopeOptions.add(options);
        return this;
    }

    public <T> GameBuilder when(Class<T> type, Consumer<? super T> listener) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        this.ensureNotBuilt();
        this.listeners.add(new ResultListener<>(type, listener));
        return this;
    }

    // public Scope build() {
    //     this.ensureNotBuilt();
    //     Class<?> type = this.type;
    //     if (type == null) {
    //         throw new IllegalStateException("type must be set before building");
    //     }
    //     this.built = true;
    //     return Scope.root(type, options -> this.scopeOptions.forEach(configure -> configure.accept(options)));
    // }

    private void ensureNotBuilt() {
        if (this.built) {
            throw new IllegalStateException("Game has already been built");
        }
    }

    private record ResultListener<T>(Class<T> type, Consumer<? super T> listener) {}
}
