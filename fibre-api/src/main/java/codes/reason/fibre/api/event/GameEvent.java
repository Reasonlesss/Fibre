package codes.reason.fibre.api.event;

import codes.reason.fibre.api.game.Game;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GameEvent {

    Game getGame();
}
