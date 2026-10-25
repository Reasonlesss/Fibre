package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.PrimaryThreadUtil;
import cloud.emilys.fibre.api.game.PreloadSink;
import cloud.emilys.fibre.core.util.ExceptionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class PreloadSinkImpl implements PreloadSink {

    private final JavaPlugin plugin;
    private final List<Entry<?>> entries = new ArrayList<>();
    private final List<Runnable> cancelActions = new ArrayList<>();

    private boolean closed;
    private boolean discarded;

    PreloadSinkImpl(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public void add(CompletionStage<?> stage) {
        this.addEntry(new Entry<>(Objects.requireNonNull(stage, "stage"), null));
    }

    @Override
    public <T> void add(CompletionStage<T> stage, Consumer<? super T> discard) {
        this.addEntry(new Entry<>(Objects.requireNonNull(stage, "stage"), Objects.requireNonNull(discard, "discard")));
    }

    @Override
    public void onCancel(Runnable action) {
        Objects.requireNonNull(action, "action");
        PrimaryThreadUtil.assertPrimary();
        this.assertOpen();
        this.cancelActions.add(action);
    }

    /** Stops accepting work and returns a stage that fails as soon as any added stage fails. */
    CompletableFuture<Void> close() {
        PrimaryThreadUtil.assertPrimary();
        this.assertOpen();
        this.closed = true;

        CompletableFuture<Void> readiness = new CompletableFuture<>();
        AtomicInteger remaining = new AtomicInteger(this.entries.size());
        if (remaining.get() == 0) {
            readiness.complete(null);
        }
        for (Entry<?> entry : this.entries) {
            entry.stage().whenComplete((_, failure) -> {
                if (failure != null) {
                    readiness.completeExceptionally(ExceptionUtil.unwrap(failure));
                } else if (remaining.decrementAndGet() == 0) {
                    readiness.complete(null);
                }
            });
        }
        return readiness;
    }

    /** Runs the cancel actions and discards every value added with a discard action. */
    void discard() {
        PrimaryThreadUtil.assertPrimary();
        if (this.discarded) {
            return;
        }
        this.closed = true;
        this.discarded = true;
        for (Runnable action : this.cancelActions) {
            this.run(action);
        }
        Executor primaryExecutor = PrimaryThreadUtil.createExecutor(this.plugin);
        for (Entry<?> entry : this.entries) {
            entry.discard(primaryExecutor, this::run);
        }
    }

    private void addEntry(Entry<?> entry) {
        PrimaryThreadUtil.assertPrimary();
        this.assertOpen();
        this.entries.add(entry);
    }

    private void assertOpen() {
        if (this.closed) {
            throw new IllegalStateException("Preload sink no longer accepts work");
        }
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            this.plugin
                    .getLogger()
                    .log(Level.WARNING, "An exception was thrown when discarding preloaded data", exception);
        }
    }

    private record Entry<T>(
            CompletionStage<T> stage, @Nullable Consumer<? super T> discard) {

        private void discard(Executor executor, Consumer<Runnable> runner) {
            Consumer<? super T> discard = this.discard;
            if (discard != null) {
                this.stage.thenAcceptAsync(value -> runner.accept(() -> discard.accept(value)), executor);
            }
        }
    }
}
