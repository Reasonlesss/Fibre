package codes.reason.fibre.core.game;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import codes.reason.fibre.api.FibreStartupRegistry;
import codes.reason.fibre.api.game.Game;
import codes.reason.fibre.api.game.GameManager;
import codes.reason.fibre.api.game.PlayerJoinToken;
import codes.reason.fibre.api.game.PlayerPreloader;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

final class PlayerJoinApiTest {

    @Test
    void exposesTokenAndUuidPreloaderContracts() {
        PlayerPreloader preloader = playerId -> CompletableFuture.completedFuture(playerId);
        BiConsumer<PlayerJoinToken, CompletionStage<?>> waitFor = PlayerJoinToken::waitFor;
        BiFunction<Game, UUID, PlayerJoinToken> tokenFactory = Game::createPlayerJoinToken;
        BiFunction<GameManager, UUID, Optional<PlayerJoinToken>> tokenFinder = GameManager::findPlayerJoinToken;
        BiConsumer<FibreStartupRegistry, PlayerPreloader> registerPreloader =
                FibreStartupRegistry::registerPlayerPreloader;

        assertNotNull(preloader);
        assertNotNull(waitFor);
        assertNotNull(tokenFactory);
        assertNotNull(tokenFinder);
        assertNotNull(registerPreloader);
    }
}
