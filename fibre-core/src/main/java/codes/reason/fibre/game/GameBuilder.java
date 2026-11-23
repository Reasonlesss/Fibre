package codes.reason.fibre.game;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.ScopeOptions;
import codes.reason.fibre.world.GameRegion;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GameBuilder {

    private final List<Consumer<? super ScopeOptions>> scopeOptions = new ArrayList<>();
    private final List<GameResultListener<?>> listeners = new ArrayList<>();
    private @Nullable Class<?> type;
    private boolean built;

    private final GameManager manager;

    GameBuilder(GameManager manager) {
        this.manager = manager;
    }

    public GameBuilder type(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        this.ensureNotBuilt();
        if (!type.isAnnotationPresent(codes.reason.fibre.scope.annotations.Game.class)) {
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
        this.listeners.add(new GameResultListener<>(type, listener));
        return this;
    }

     public Game build() {
         this.ensureNotBuilt();
         Class<?> type = this.type;
         if (type == null) {
             throw new IllegalStateException("type must be set before building");
         }
         this.built = true;
         Scope scope = Scope.root(type, options -> {
             // Initialize game components
             GameIdentity identity = GameIdentity.random(type);
             GameExecutor executor = new GameExecutor(Fibre.get().platform().logger());
             GameEvents events = new GameEvents(executor);
             GamePlayers players = new GamePlayers(events);
             GameSnapshots snapshots = new GameSnapshots(this.manager.states());
             GameLifecycle lifecycle = new GameLifecycle(
                     options.handle(),
                     this.manager,
                     identity,
                     executor,
                     events,
                     players,
                     this.listeners
             );

             // Bind components
             options.bind(GameIdentity.class, identity);
             options.bind(GameExecutor.class, executor);
             options.bind(GameLifecycle.class, lifecycle);
             options.bind(GameEvents.class, events);
             options.bind(GamePlayers.class, players);
             options.bind(GameSnapshots.class, snapshots);
             options.bind(GameManager.class, this.manager);

             // Install global plugins, before any provided ones so they run first
             this.manager.plugins().plugins().forEach(options::plugin);

             // Apply provided scope objects
             this.scopeOptions.forEach(configure -> configure.accept(options));
             if (!options.isBound(GameRegion.class)) {
                 throw new IllegalStateException("An unnamed GameRegion must be bound");
             }
             // checked before the scope is built, so a game overlapping a running one never enters
             this.manager.ensureNoOverlap(identity, options.values(GameRegion.class));
         });
         Game game = new Game(scope, this.manager);
         if (!game.lifecycle().isClosed()) {
             try {
                 this.manager.register(game);
             } catch (RuntimeException failure) {
                 // a game that can't be registered never runs, so it's closed before the failure is thrown
                 try {
                     game.lifecycle().close();
                 } catch (RuntimeException closeFailure) {
                     failure.addSuppressed(closeFailure);
                 }
                 throw failure;
             }
         }
         return game;
     }

    private void ensureNotBuilt() {
        if (this.built) {
            throw new IllegalStateException("Game has already been built");
        }
    }

}
