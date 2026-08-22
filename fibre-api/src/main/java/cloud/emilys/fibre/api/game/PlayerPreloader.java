package cloud.emilys.fibre.api.game;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NullMarked;

/** Starts work that must finish before a reserved player is joined to a game. */
@FunctionalInterface
@NullMarked
public interface PlayerPreloader {

    CompletableFuture<?> preload(UUID playerId);
}
