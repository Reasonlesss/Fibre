package cloud.emilys.fibre.core.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.game.Game;
import java.lang.reflect.Proxy;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class PlayerJoinReservationTest {

    @Test
    void cancellationAllowsReplacementWithoutLettingStaleTokenRemoveIt() {
        GameManagerImpl manager = new GameManagerImpl(proxy(Fibre.class), _ -> {});
        UUID playerId = UUID.randomUUID();
        Game game = proxy(Game.class);
        PlayerJoinTokenImpl first = token(manager, game, playerId);
        PlayerJoinTokenImpl replacement = token(manager, game, playerId);

        assertTrue(manager.reservePlayerJoinToken(first));
        assertFalse(manager.reservePlayerJoinToken(replacement));

        first.cancel();
        assertTrue(manager.reservePlayerJoinToken(replacement));
        manager.releasePlayerJoinToken(first);

        assertSame(replacement, manager.findPlayerJoinToken(playerId).orElseThrow());
    }

    private static PlayerJoinTokenImpl token(GameManagerImpl manager, Game game, UUID playerId) {
        return new PlayerJoinTokenImpl(game, playerId, manager::releasePlayerJoinToken, (_, _) -> {});
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (_, method, _) -> {
            throw new UnsupportedOperationException(method.getName());
        });
    }
}
