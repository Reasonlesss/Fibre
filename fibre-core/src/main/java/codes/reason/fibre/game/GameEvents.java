package codes.reason.fibre.game;

import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.event.GameEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GameEvents {

    private final GameExecutor executor;
    private final List<Registration<?>> registrations = new ArrayList<>();

    GameEvents(GameExecutor executor) {
        this.executor = executor;
    }

    public void call(GameEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        for (Registration<?> registration : List.copyOf(this.registrations)) {
            if (registration.isActive()) {
                this.executor.execute(() -> registration.accept(event));
            }
        }
    }

    public <E extends GameEvent> Registration<E> register(Class<E> type, Consumer<? super E> listener) {
        return this.register(type, EventOrder.NORMAL, listener);
    }

    public <E extends GameEvent> Registration<E> register(Class<E> type, EventOrder order, Consumer<? super E> listener) {
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

    public static final class Registration<E extends GameEvent> implements AutoCloseable {

        private final GameEvents events;
        private final Class<E> type;
        private final EventOrder order;
        private final Consumer<? super E> listener;
        private boolean active = true;

        private Registration(GameEvents events, Class<E> type, EventOrder order, Consumer<? super E> listener) {
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

        private void accept(GameEvent event) {
            if (this.type.isInstance(event)) {
                this.listener.accept(this.type.cast(event));
            }
        }

    }

}
