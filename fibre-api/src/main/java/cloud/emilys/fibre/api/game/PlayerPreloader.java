package cloud.emilys.fibre.api.game;

import java.util.UUID;
import org.jspecify.annotations.NullMarked;

/** Starts deferred work that must finish before a reserved player is joined to a game. */
@FunctionalInterface
@NullMarked
public interface PlayerPreloader {

    void preload(Game game, UUID playerId, PreloadSink sink);
}
