package cloud.emilys.fibre.api.game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GameManager {

    GameBuilder createGame();

    List<Game> getGames();

    Optional<Game> findGame(Object object);

    /** Returns the uncommitted join token reserved for the player, if any. */
    Optional<PlayerJoinToken> findPlayerJoinToken(UUID playerId);
}
