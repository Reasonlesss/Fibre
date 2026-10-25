package cloud.emilys.fibre.api.game;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

/**
 * Collects the work a player join waits on before it is ready to commit.
 *
 * <p>A sink only accepts work while the join is being created. Stages added to it are waited on but never cancelled,
 * so a stage shared with other code is safe to add. When a join is cancelled or fails, cleanup is done through the
 * discard actions and cancel actions registered here, always on the primary server thread.
 */
@NullMarked
public interface PreloadSink {

    /** Waits for {@code stage} before the join is ready. The stage is never cancelled by Fibre. */
    void add(CompletionStage<?> stage);

    /**
     * Waits for {@code stage} before the join is ready. If the join is cancelled or fails instead of committing,
     * {@code discard} receives the stage's value, whether it completed before or after that happened. Only pass a
     * discard action for values the join owns.
     */
    <T> void add(CompletionStage<T> stage, Consumer<? super T> discard);

    /** Runs {@code action} once if the join is cancelled or fails instead of committing. */
    void onCancel(Runnable action);
}
