package cloud.emilys.fibre.core.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cloud.emilys.fibre.api.game.Game;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

final class PlayerJoinTokenImplTest {

    private static final Game GAME = proxy(Game.class, UUID.randomUUID());

    @Test
    void awaitBlocksUntilEveryAttachedStageCompletes() throws Exception {
        CompletableFuture<Void> first = new CompletableFuture<>();
        CompletableFuture<Void> second = new CompletableFuture<>();
        PlayerJoinTokenImpl token = token(UUID.randomUUID(), new AtomicInteger(), new AtomicInteger());
        token.waitFor(first);
        token.waitFor(second);

        CompletableFuture<Void> waiting = CompletableFuture.runAsync(token::await);
        first.complete(null);

        assertFalse(waiting.isDone());
        second.complete(null);
        waiting.get(1, TimeUnit.SECONDS);
    }

    @Test
    void cancelUnblocksAwaitWithoutCancellingAttachedStages() {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        AtomicInteger releases = new AtomicInteger();
        PlayerJoinTokenImpl token = token(UUID.randomUUID(), releases, new AtomicInteger());
        token.waitFor(stage);
        CompletableFuture<Void> waiting = CompletableFuture.runAsync(token::await);

        token.cancel();
        token.cancel();

        CompletionException failure = assertThrows(CompletionException.class, waiting::join);
        assertSame(CancellationException.class, failure.getCause().getClass());
        assertFalse(stage.isCancelled());
        assertEquals(1, releases.get());
    }

    @Test
    void cancelBeforeAwaitFailsFutureAwaitCalls() {
        AtomicInteger releases = new AtomicInteger();
        PlayerJoinTokenImpl token = token(UUID.randomUUID(), releases, new AtomicInteger());

        token.cancel();

        assertThrows(CancellationException.class, token::await);
        assertEquals(1, releases.get());
    }

    @Test
    void failedStageReleasesReservationAndPreservesFailure() {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        AtomicInteger releases = new AtomicInteger();
        PlayerJoinTokenImpl token = token(UUID.randomUUID(), releases, new AtomicInteger());
        token.waitFor(stage);
        IllegalArgumentException expected = new IllegalArgumentException("load failed");
        stage.completeExceptionally(expected);

        CompletionException failure = assertThrows(CompletionException.class, token::await);

        assertSame(expected, failure.getCause());
        assertEquals(1, releases.get());
    }

    @Test
    void cancelledStageFailsTokenAndReleasesReservation() {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        AtomicInteger releases = new AtomicInteger();
        PlayerJoinTokenImpl token = token(UUID.randomUUID(), releases, new AtomicInteger());
        token.waitFor(stage);
        stage.cancel(false);

        CompletionException failure = assertThrows(CompletionException.class, token::await);

        assertSame(CancellationException.class, failure.getCause().getClass());
        assertEquals(1, releases.get());
    }

    @Test
    void joinWaitsBeforeInvokingCompletion() throws Exception {
        UUID playerId = UUID.randomUUID();
        CompletableFuture<Void> stage = new CompletableFuture<>();
        AtomicInteger joins = new AtomicInteger();
        PlayerJoinTokenImpl token = token(playerId, new AtomicInteger(), joins);
        token.waitFor(stage);

        CompletableFuture<Void> joining = CompletableFuture.runAsync(() -> token.join(player(playerId)));

        assertEquals(0, joins.get());
        stage.complete(null);
        joining.get(1, TimeUnit.SECONDS);
        assertEquals(1, joins.get());
    }

    @Test
    void joinRejectsPlayerWithAnotherUuid() {
        PlayerJoinTokenImpl token = token(UUID.randomUUID(), new AtomicInteger(), new AtomicInteger());

        assertThrows(IllegalArgumentException.class, () -> token.join(player(UUID.randomUUID())));
    }

    @Test
    void joinRunsOnlyOnce() {
        UUID playerId = UUID.randomUUID();
        AtomicInteger joins = new AtomicInteger();
        PlayerJoinTokenImpl token = token(playerId, new AtomicInteger(), joins);
        Player player = player(playerId);

        token.join(player);

        assertThrows(IllegalStateException.class, () -> token.join(player));
        assertEquals(1, joins.get());
    }

    @Test
    void waitForRejectsStagesAfterAwaitSealsToken() {
        PlayerJoinTokenImpl token = token(UUID.randomUUID(), new AtomicInteger(), new AtomicInteger());
        token.await();

        assertThrows(IllegalStateException.class, () -> token.waitFor(CompletableFuture.completedFuture(null)));
    }

    private static PlayerJoinTokenImpl token(UUID playerId, AtomicInteger releases, AtomicInteger joins) {
        return new PlayerJoinTokenImpl(
                GAME, playerId, _ -> releases.incrementAndGet(), (_, _) -> joins.incrementAndGet());
    }

    private static Player player(UUID playerId) {
        return proxy(Player.class, playerId);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, UUID playerId) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (_, method, _) -> {
            if (method.getName().equals("getUniqueId")) {
                return playerId;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }
}
