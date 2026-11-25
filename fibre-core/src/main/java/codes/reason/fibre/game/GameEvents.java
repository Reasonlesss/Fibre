package codes.reason.fibre.game;

import codes.reason.fibre.Registration;
import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.event.GameEvent;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GameEvents {

    private final GameExecutor executor;
    private final FibreEvents global;
    private final EventListeners listeners = new EventListeners();
    // set once the game exists; global listeners are told which game an event came from
    private @Nullable GameHolder game;

    GameEvents(GameExecutor executor, FibreEvents global) {
        this.executor = executor;
        this.global = global;
    }

    void attach(GameHolder game) {
        this.game = game;
    }

    // false if any listener threw
    public boolean call(GameEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        GameHolder game = this.game;
        List<EventListeners.Entry> local = this.listeners.snapshot();
        // global listeners need the game, so none run before it exists
        List<EventListeners.Entry> global = game != null ? this.global.listeners().snapshot() : List.of();
        boolean completed = true;
        // both lists are sorted by order; on a tie the game's own listeners go first
        int l = 0;
        int g = 0;
        while (l < local.size() || g < global.size()) {
            boolean takeLocal = g >= global.size()
                    || (l < local.size() && local.get(l).order().compareTo(global.get(g).order()) <= 0);
            EventListeners.Entry entry = takeLocal ? local.get(l++) : global.get(g++);
            if (entry.isRegistered()) {
                completed &= this.executor.execute(() -> entry.accept(game, event));
            }
        }
        return completed;
    }

    public <E extends GameEvent> Registration register(Class<E> type, Listener<? super E> listener) {
        return this.register(type, EventOrder.NORMAL, listener);
    }

    public <E extends GameEvent> Registration register(Class<E> type, EventOrder order, Listener<? super E> listener) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        return this.listeners.add(type, order, (_, event) -> listener.accept(event));
    }

    void closeAll() {
        this.listeners.closeAll();
    }

    @FunctionalInterface
    public interface Listener<E extends GameEvent> {

        void accept(E event) throws Throwable;

    }

}
