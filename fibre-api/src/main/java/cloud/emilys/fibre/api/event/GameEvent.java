package cloud.emilys.fibre.api.event;

import cloud.emilys.fibre.api.game.Game;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GameEvent {

    Game getGame();
}
