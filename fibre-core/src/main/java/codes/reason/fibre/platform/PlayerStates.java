package codes.reason.fibre.platform;

import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.player.PlayerSnapshot;
import codes.reason.fibre.world.WorldPosition;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlayerStates {

    PlayerSnapshot capture(PlayerAdapter<?> player);

    void restore(PlayerAdapter<?> player, PlayerSnapshot snapshot);

    void reset(PlayerAdapter<?> player);

    // moves the player there, or if they're dead, makes it where they respawn
    void place(PlayerAdapter<?> player, WorldPosition position);

}
