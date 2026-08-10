package cloud.emilys.fibre.api.game;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GameManager {

    GameBuilder createGame();

    List<Game> getGames();

    Optional<Game> findGame(Object object);
}
