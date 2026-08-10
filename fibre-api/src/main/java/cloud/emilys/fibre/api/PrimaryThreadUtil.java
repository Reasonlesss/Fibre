package cloud.emilys.fibre.api;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PrimaryThreadUtil {

    private PrimaryThreadUtil() {
        throw new UnsupportedOperationException();
    }

    public static Executor createExecutor(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        return runnable -> PrimaryThreadUtil.ensureMainThread(plugin, runnable);
    }

    public static void assertPrimary() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Method called outside of primary server thread");
        }
    }

    public static void ensureMainThread(Plugin plugin, Runnable runnable) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(runnable, "runnable");
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
