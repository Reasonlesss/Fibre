package codes.reason.fibre.event;

import codes.reason.fibre.player.PlayerAdapter;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record GamePlayerAddEvent(PlayerAdapter<?> player) implements GamePlayerEvent {

    public GamePlayerAddEvent {
        Objects.requireNonNull(player, "player must not be null");
    }

}
