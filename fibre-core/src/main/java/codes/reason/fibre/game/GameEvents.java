package codes.reason.fibre.game;

import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.event.GameEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GameEvents {

    private final GameExecutor executor;
    private final List<Registration<?>> registrations = new ArrayList<>();

    GameEvents(GameExecutor executor) {
        this.executor = executor;
    }

    // false if any listener threw
    public boolean call(GameEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        boolean completed = true;
        for (Registration<?> registration : List.copyOf(this.registrations)) {
            if (registration.isActive()) {
                completed &= this.executor.execute(() -> registration.accept(event));
            }
        }
        return completed;
    }

    public <E extends GameEvent> Registration<E> register(Class<E> type, Listener<? super E> listener) {
        return this.register(type, EventOrder.NORMAL, listener);
    }

    public <E extends GameEvent> Registration<E> register(Class<E> type, EventOrder order, Listener<? super E> listener) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        Registration<E> registration = new Registration<>(this, type, order, listener);
        // after every listener of the same order, so ties run in registration order
        int index = 0;
        while (index < this.registrations.size() && this.registrations.get(index).order.compareTo(order) <= 0) {
            index++;
        }
        this.registrations.add(index, registration);
        return registration;
    }

    void closeAll() {
        for (Registration<?> registration : List.copyOf(this.registrations)) {
            registration.close();
        }
    }

    @FunctionalInterface
    public interface Listener<E extends GameEvent> {

        void accept(E event) throws Throwable;

    }

    public static final class Registration<E extends GameEvent> implements AutoCloseable {

        private final GameEvents events;
        private final Class<E> type;
        private final EventOrder order;
        private final Listener<? super E> listener;
        private boolean active = true;

        private Registration(GameEvents events, Class<E> type, EventOrder order, Listener<? super E> listener) {
            this.events = events;
            this.type = type;
            this.order = order;
            this.listener = listener;
        }

        public boolean isActive() {
            return this.active;
        }

        @Override
        public void close() {
            this.active = false;
            this.events.registrations.remove(this);
        }

        private void accept(GameEvent event) throws Throwable {
            if (this.type.isInstance(event)) {
                this.listener.accept(this.type.cast(event));
            }
        }

    }

}
