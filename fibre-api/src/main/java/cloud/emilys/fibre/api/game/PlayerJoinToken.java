package cloud.emilys.fibre.api.game;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * A reserved place in a game for a player whose data is being preloaded.
 *
 * <p>A token does nothing on its own once ready: call {@link #commitWhenReady()} to let Fibre join the player as soon
 * as they are ready and online, or {@link #commit()} to join them yourself. Every method must be called on the primary
 * server thread, and callbacks run on it.
 */
@NullMarked
public interface PlayerJoinToken {

    Game getGame();

    UUID getPlayerId();

    /** Completes once preloading has finished, or exceptionally if it fails or the token is cancelled first. */
    CompletionStage<Void> ready();

    /** Completes with the player once they have joined the game, or exceptionally if the token never commits. */
    CompletionStage<Player> committed();

    /** Runs {@code action} once the player has joined the game. */
    PlayerJoinToken onCommit(Consumer<? super Player> action);

    /** Runs {@code action} if the token is cancelled or fails instead of committing. */
    PlayerJoinToken onFailure(Consumer<? super Throwable> action);

    /**
     * Joins the player once preloading has finished. The returned stage fails without cancelling the token if the
     * player is not online at that point, so the commit can be retried.
     */
    CompletionStage<Player> commit();

    /** Joins the player as soon as preloading has finished and they are online. */
    PlayerJoinToken commitWhenReady();

    /**
     * Holds the player's login until preloading has finished, and refuses it if preloading fails. Without this, a player
     * can log in while their data is still loading.
     */
    PlayerJoinToken deferLogin();

    /**
     * Cancels the join and discards its preloaded data.
     *
     * @return {@code false} if the token has already committed, been cancelled or failed
     */
    boolean cancel();
}
