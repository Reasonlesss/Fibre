package codes.reason.fibre.event;

import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// fired once as a game shuts down, while its players are still in it; the game is already closed, so it takes no one new
@NullMarked
public final class GameTerminateEvent implements GameEvent {

    private final @Nullable Object result;

    public GameTerminateEvent(@Nullable Object result) {
        this.result = result;
    }

    // what the game completed with, or empty if it was closed without one
    public Optional<Object> result() {
        return Optional.ofNullable(this.result);
    }

    public <T> Optional<T> result(Class<T> type) {
        return this.result().filter(type::isInstance).map(type::cast);
    }

}
