package codes.reason.fibre.game;

import codes.reason.fibre.Registration;
import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.event.GameEvent;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

// every game's events, for code outside any game; listeners run alongside each game's own, by order
@NullMarked
public final class FibreEvents {

    private final EventListeners listeners = new EventListeners();

    FibreEvents() {}

    public <E extends GameEvent> Registration register(Class<E> type, Listener<? super E> listener) {
        return this.register(type, EventOrder.NORMAL, listener);
    }

    public <E extends GameEvent> Registration register(Class<E> type, EventOrder order, Listener<? super E> listener) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        // GameEvents only calls these once the game exists
        return this.listeners.add(type, order, (game, event) -> listener.accept(Objects.requireNonNull(game), event));
    }

    EventListeners listeners() {
        return this.listeners;
    }

    @FunctionalInterface
    public interface Listener<E extends GameEvent> {

        void accept(GameHolder game, E event) throws Throwable;

    }

}
