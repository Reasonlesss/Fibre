package cloud.emilys.fibre.features.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import cloud.emilys.fibre.api.game.PlayerPreloader;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

final class PlayerPreloadFeatureTest {

    @Test
    void attachesEveryPreloaderFutureForThePlayerUuid() throws Exception {
        UUID playerId = UUID.randomUUID();
        CompletableFuture<Void> first = new CompletableFuture<>();
        CompletableFuture<Void> second = new CompletableFuture<>();
        AtomicInteger calls = new AtomicInteger();
        PlayerJoinToken token = token(playerId);
        List<PlayerPreloader> preloaders = List.of(
                id -> {
                    assertEquals(playerId, id);
                    calls.incrementAndGet();
                    return first;
                },
                id -> {
                    assertEquals(playerId, id);
                    calls.incrementAndGet();
                    return second;
                });

        PlayerPreloadFeature.attachPreloads(preloaders, token);
        CompletableFuture<Void> waiting = CompletableFuture.runAsync(token::await);
        first.complete(null);

        assertFalse(waiting.isDone());
        second.complete(null);
        waiting.get(1, TimeUnit.SECONDS);
        assertEquals(2, calls.get());
    }

    @Test
    void rejectsNullPreloaderFuture() {
        PlayerJoinToken token = token(UUID.randomUUID());

        assertThrows(NullPointerException.class, () -> PlayerPreloadFeature.attachPreloads(List.of(_ -> null), token));
    }

    private static PlayerJoinToken token(UUID playerId) {
        Game game = proxy(Game.class);
        return new TestPlayerJoinToken(game, playerId);
    }

    private static final class TestPlayerJoinToken implements PlayerJoinToken {

        private final Game game;
        private final UUID playerId;
        private final java.util.ArrayList<java.util.concurrent.CompletionStage<?>> stages = new java.util.ArrayList<>();

        private TestPlayerJoinToken(Game game, UUID playerId) {
            this.game = game;
            this.playerId = playerId;
        }

        @Override
        public Game getGame() {
            return this.game;
        }

        @Override
        public UUID getPlayerId() {
            return this.playerId;
        }

        @Override
        public void waitFor(java.util.concurrent.CompletionStage<?> stage) {
            this.stages.add(stage);
        }

        @Override
        public void await() {
            CompletableFuture.allOf(this.stages.stream()
                            .map(java.util.concurrent.CompletionStage::toCompletableFuture)
                            .toArray(CompletableFuture<?>[]::new))
                    .join();
        }

        @Override
        public void join(Player player) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancel() {
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (_, method, _) -> {
            throw new UnsupportedOperationException(method.getName());
        });
    }
}
