package codes.reason.fibre.platform;

import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.player.PlayerSnapshot;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlayerStates {

    PlayerSnapshot capture(PlayerAdapter<?> player);

    void restore(PlayerAdapter<?> player, PlayerSnapshot snapshot);

    void reset(PlayerAdapter<?> player);

}
