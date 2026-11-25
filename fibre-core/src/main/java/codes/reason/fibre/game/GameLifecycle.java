package codes.reason.fibre.game;

import codes.reason.fibre.event.GameTerminateEvent;
import codes.reason.fibre.scope.Handle;
import codes.reason.fibre.scope.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GameLifecycle {

    private final Handle<Scope> scope;
    private final GameManager manager;
    private final GameIdentity identity;
    private final GameExecutor executor;
    private final GameEvents events;
    private final GamePlayers players;
    private final List<GameResultListener<?>> listeners;
    private boolean closed;

    GameLifecycle(Handle<Scope> scope, GameManager manager, GameIdentity identity, GameExecutor executor, GameEvents events, GamePlayers players, List<GameResultListener<?>> listeners) {
        this.scope = scope;
        this.manager = manager;
        this.identity = identity;
        this.executor = executor;
        this.events = events;
        this.players = players;
        this.listeners = List.copyOf(listeners);
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void complete(Object result) {
        Objects.requireNonNull(result, "result must not be null");
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            for (GameResultListener<?> listener : this.listeners) {
                this.executor.execute(() -> listener.invoke(result));
            }
        } finally {
            this.shutdown(result);
        }
    }

    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.shutdown(null);
    }

    private void shutdown(@Nullable Object result) {
        List<Throwable> failures = new ArrayList<>();
        // before anyone leaves, so listeners still see who was playing
        step(failures, () -> this.events.call(new GameTerminateEvent(result)));
        // players get back their state from before the game, then the closed game takes no more
        step(failures, () -> this.manager.find(this.identity.id()).ifPresent(this.manager::release));
        step(failures, this.players::close);
        step(failures, this.events::closeAll);
        step(failures, () -> this.scope.require().close());
        step(failures, () -> this.manager.unregister(this.identity.id()));
        if (!failures.isEmpty()) {
            IllegalStateException exception = new IllegalStateException(
                    "Failed to close game %s cleanly".formatted(this.identity));
            failures.forEach(exception::addSuppressed);
            throw exception;
        }
    }

    private static void step(List<Throwable> failures, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            failures.add(t);
        }
    }

}
