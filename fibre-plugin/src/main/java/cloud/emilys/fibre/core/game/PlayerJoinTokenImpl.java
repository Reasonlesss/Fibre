package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.PrimaryThreadUtil;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import cloud.emilys.fibre.core.util.ExceptionUtil;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

// State is only read and written on the primary thread; preload stages hop back to it before touching the token.
@NullMarked
final class PlayerJoinTokenImpl implements PlayerJoinToken {

    private final GameImpl game;
    private final GameManagerImpl manager;
    private final UUID playerId;
    private final Executor primaryExecutor;
    private final PreloadSinkImpl sink;
    private final CompletableFuture<Void> readiness = new CompletableFuture<>();
    private final CompletableFuture<Player> committed = new CompletableFuture<>();

    private State state = State.PRELOADING;
    private boolean autoCommit;

    PlayerJoinTokenImpl(GameImpl game, GameManagerImpl manager, UUID playerId) {
        this.game = Objects.requireNonNull(game, "game");
        this.manager = Objects.requireNonNull(manager, "manager");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.primaryExecutor = PrimaryThreadUtil.createExecutor(game.getPlugin());
        this.sink = new PreloadSinkImpl(game.getPlugin());
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
    public CompletionStage<Void> ready() {
        return this.readiness.minimalCompletionStage();
    }

    @Override
    public CompletionStage<Player> committed() {
        return this.committed.minimalCompletionStage();
    }

    @Override
    public PlayerJoinToken onCommit(Consumer<? super Player> action) {
        Objects.requireNonNull(action, "action");
        this.committed.thenAcceptAsync(player -> this.runCallback(() -> action.accept(player)), this.primaryExecutor);
        return this;
    }

    @Override
    public PlayerJoinToken onFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action");
        this.committed.whenCompleteAsync(
                (_, failure) -> {
                    if (failure != null) {
                        this.runCallback(() -> action.accept(ExceptionUtil.unwrap(failure)));
                    }
                },
                this.primaryExecutor);
        return this;
    }

    @Override
    public CompletionStage<Player> commit() {
        PrimaryThreadUtil.assertPrimary();
        return this.manager.commit(this);
    }

    @Override
    public PlayerJoinToken commitWhenReady() {
        PrimaryThreadUtil.assertPrimary();
        if (this.isPending() && !this.autoCommit) {
            this.autoCommit = true;
            this.manager.joinWhenReady(this);
        }
        return this;
    }

    @Override
    public boolean cancel() {
        PrimaryThreadUtil.assertPrimary();
        if (this.state != State.PRELOADING && this.state != State.WAITING) {
            return false;
        }
        this.state = State.CANCELLED;
        this.finish(new CancellationException("Player join token was cancelled"));
        return true;
    }

    PreloadSinkImpl getSink() {
        return this.sink;
    }

    boolean isPending() {
        return this.state == State.PRELOADING || this.state == State.WAITING;
    }

    boolean isAutoCommit() {
        return this.autoCommit;
    }

    /** Stops accepting preload work and starts waiting for the work already added. */
    void seal() {
        PrimaryThreadUtil.assertPrimary();
        if (this.state != State.PRELOADING) {
            throw new IllegalStateException("Player join token cannot be sealed from state " + this.state);
        }
        this.state = State.WAITING;
        this.sink
                .close()
                .whenCompleteAsync(
                        (_, failure) -> {
                            if (failure == null) {
                                this.readiness.complete(null);
                            } else {
                                this.fail(failure);
                            }
                        },
                        this.primaryExecutor);
    }

    void beginJoin(Player player) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(player, "player");
        if (!this.playerId.equals(player.getUniqueId())) {
            throw new IllegalArgumentException("Player UUID does not match this join token");
        }
        if (this.state != State.WAITING || !this.readiness.isDone() || this.readiness.isCompletedExceptionally()) {
            throw new IllegalStateException("Player join token cannot be joined from state " + this.state);
        }
        this.state = State.JOINING;
    }

    void joined(Player player) {
        PrimaryThreadUtil.assertPrimary();
        if (this.state != State.JOINING) {
            throw new IllegalStateException("Player join token cannot complete from state " + this.state);
        }
        this.state = State.JOINED;
        this.committed.complete(player);
    }

    void fail(Throwable failure) {
        PrimaryThreadUtil.assertPrimary();
        if (this.state == State.JOINED || this.state == State.CANCELLED || this.state == State.FAILED) {
            return;
        }
        Throwable cause = ExceptionUtil.unwrap(failure);
        String message = this.state == State.JOINING
                ? "Could not join player " + this.playerId + " to the game"
                : "Could not prepare game data for player " + this.playerId;
        this.state = State.FAILED;
        this.game.getPlugin().getLogger().log(Level.SEVERE, message, cause);
        this.finish(cause);
    }

    private void finish(Throwable failure) {
        this.readiness.completeExceptionally(failure);
        this.committed.completeExceptionally(failure);
        this.sink.discard();
        this.manager.releaseToken(this);
    }

    private void runCallback(Runnable callback) {
        try {
            callback.run();
        } catch (RuntimeException exception) {
            this.game
                    .getPlugin()
                    .getLogger()
                    .log(Level.WARNING, "An exception was thrown by a player join callback", exception);
        }
    }

    private enum State {
        PRELOADING,
        WAITING,
        JOINING,
        JOINED,
        CANCELLED,
        FAILED
    }
}
