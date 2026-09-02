package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class PlayerJoinTokenImpl implements PlayerJoinToken {

    private final Game game;
    private final UUID playerId;
    private final Consumer<PlayerJoinTokenImpl> releaseAction;
    private final Object lock = new Object();
    private final List<CompletionStage<?>> stages = new ArrayList<>();
    private final AtomicBoolean released = new AtomicBoolean();

    private State state = State.PENDING;
    private @Nullable CompletableFuture<Void> readiness;

    PlayerJoinTokenImpl(Game game, UUID playerId, Consumer<PlayerJoinTokenImpl> releaseAction) {
        this.game = Objects.requireNonNull(game, "game");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.releaseAction = Objects.requireNonNull(releaseAction, "releaseAction");
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
    public void waitFor(CompletionStage<?> stage) {
        Objects.requireNonNull(stage, "stage");
        synchronized (this.lock) {
            if (this.state != State.PENDING) {
                throw new IllegalStateException("Player join token has already been sealed");
            }
            this.stages.add(stage);
        }
    }

    void beginJoin(Player player) {
        Objects.requireNonNull(player, "player");
        if (!this.playerId.equals(player.getUniqueId())) {
            throw new IllegalArgumentException("Player UUID does not match this join token");
        }

        this.seal().join();
        synchronized (this.lock) {
            if (this.state != State.WAITING) {
                throw new IllegalStateException("Player join token cannot be joined from state " + this.state);
            }
            this.state = State.JOINING;
        }
    }

    void joined() {
        synchronized (this.lock) {
            if (this.state != State.JOINING) {
                throw new IllegalStateException("Player join token cannot complete from state " + this.state);
            }
            this.state = State.JOINED;
        }
    }

    void failed() {
        synchronized (this.lock) {
            this.state = State.FAILED;
        }
        this.release();
    }

    CompletableFuture<Void> readiness() {
        return this.seal();
    }

    @Override
    public void cancel() {
        CompletableFuture<Void> completion;
        CancellationException failure = new CancellationException("Player join token was cancelled");
        synchronized (this.lock) {
            if (this.state == State.CANCELLED || this.state == State.FAILED) {
                return;
            }
            if (this.state == State.JOINING || this.state == State.JOINED) {
                throw new IllegalStateException("A joined player cannot be cancelled");
            }
            if (this.state == State.PENDING) {
                completion = new CompletableFuture<>();
                this.readiness = completion;
                this.state = State.CANCELLED;
            } else {
                this.state = State.CANCELLED;
                completion = Objects.requireNonNull(this.readiness, "readiness");
            }
            if (!completion.completeExceptionally(failure)) {
                completion = CompletableFuture.failedFuture(failure);
                this.readiness = completion;
            }
        }
        this.release();
    }

    private CompletableFuture<Void> seal() {
        synchronized (this.lock) {
            return switch (this.state) {
                case PENDING -> this.createReadiness(State.WAITING);
                case WAITING, JOINING, JOINED, CANCELLED, FAILED -> Objects.requireNonNull(this.readiness, "readiness");
            };
        }
    }

    private CompletableFuture<Void> createReadiness(State newState) {
        CompletableFuture<?>[] futures = this.stages.stream()
                .map(stage -> stage.handle((_, failure) -> {
                            if (failure != null) {
                                throw new java.util.concurrent.CompletionException(unwrap(failure));
                            }
                            return null;
                        })
                        .toCompletableFuture())
                .toArray(CompletableFuture<?>[]::new);
        CompletableFuture<Void> completion = CompletableFuture.allOf(futures);
        this.readiness = completion;
        this.state = newState;
        completion.whenComplete((_, failure) -> {
            if (failure == null) {
                return;
            }
            boolean failed = false;
            synchronized (this.lock) {
                if (this.state == State.WAITING) {
                    this.state = State.FAILED;
                    failed = true;
                }
            }
            if (failed) {
                this.release();
            }
        });
        return completion;
    }

    private void release() {
        if (this.released.compareAndSet(false, true)) {
            this.releaseAction.accept(this);
        }
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable result = failure;
        while (result instanceof java.util.concurrent.CompletionException && result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

    private enum State {
        PENDING,
        WAITING,
        JOINING,
        JOINED,
        CANCELLED,
        FAILED
    }
}
