package codes.reason.fibre.event;

import codes.reason.fibre.player.PlayerAdapter;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// fired before a player is added, while nothing about them has changed yet; a denied join leaves them where they were
@NullMarked
public final class GamePlayerJoinEvent implements GamePlayerEvent {

    private final PlayerAdapter<?> player;
    private @Nullable String denial;

    public GamePlayerJoinEvent(PlayerAdapter<?> player) {
        this.player = Objects.requireNonNull(player, "player must not be null");
    }

    @Override
    public PlayerAdapter<?> player() {
        return this.player;
    }

    public void deny(String reason) {
        this.denial = Objects.requireNonNull(reason, "reason must not be null");
    }

    // a later listener can overturn an earlier denial
    public void allow() {
        this.denial = null;
    }

    public boolean denied() {
        return this.denial != null;
    }

    public Optional<String> denial() {
        return Optional.ofNullable(this.denial);
    }

}
