package codes.reason.fibre.event;

import codes.reason.fibre.player.PlayerAdapter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GamePlayerEvent extends GameEvent {

    PlayerAdapter<?> player();

}
