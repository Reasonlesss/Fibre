package codes.reason.fibre.game;

import codes.reason.fibre.Registration;
import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.event.GameEvent;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// listeners kept sorted by order, shared by a game's own events and the global ones
@NullMarked
final class EventListeners {

    private final List<Entry> entries = new ArrayList<>();

    <E extends GameEvent> Registration add(Class<E> type, EventOrder order, Dispatch<E> dispatch) {
        Entry entry = new Entry(type, order, dispatch);
        // after every listener of the same order, so ties run in registration order
        int index = 0;
        while (index < this.entries.size() && this.entries.get(index).order.compareTo(order) <= 0) {
            index++;
        }
        this.entries.add(index, entry);
        return entry;
    }

    List<Entry> snapshot() {
        return List.copyOf(this.entries);
    }

    void closeAll() {
        for (Entry entry : this.snapshot()) {
            entry.unregister();
        }
    }

    @FunctionalInterface
    interface Dispatch<E extends GameEvent> {

        // null only for a game's own listeners, before the game exists
        void accept(@Nullable GameHolder game, E event) throws Throwable;

    }

    final class Entry implements Registration {

        private final Class<? extends GameEvent> type;
        private final EventOrder order;
        private final Dispatch<?> dispatch;
        private boolean registered = true;

        private Entry(Class<? extends GameEvent> type, EventOrder order, Dispatch<?> dispatch) {
            this.type = type;
            this.order = order;
            this.dispatch = dispatch;
        }

        EventOrder order() {
            return this.order;
        }

        @Override
        public boolean isRegistered() {
            return this.registered;
        }

        @Override
        public void unregister() {
            this.registered = false;
            EventListeners.this.entries.remove(this);
        }

        @SuppressWarnings("unchecked")
        void accept(@Nullable GameHolder game, GameEvent event) throws Throwable {
            if (this.type.isInstance(event)) {
                ((Dispatch<GameEvent>) this.dispatch).accept(game, event);
            }
        }

    }

}
