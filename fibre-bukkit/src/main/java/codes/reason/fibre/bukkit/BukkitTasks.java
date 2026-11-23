package codes.reason.fibre.bukkit;

import codes.reason.fibre.platform.PlatformRegistration;
import codes.reason.fibre.platform.Scheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class BukkitTasks implements Scheduler {

    private final Plugin plugin;

    BukkitTasks(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PlatformRegistration run(Runnable task) {
        return this.register(this.plugin.getServer().getScheduler().runTask(this.plugin, task));
    }

    @Override
    public PlatformRegistration runLater(long delayTicks, Runnable task) {
        return this.register(this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, delayTicks));
    }

    @Override
    public PlatformRegistration runRepeating(long delayTicks, long periodTicks, Runnable task) {
        return this.register(this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, task, delayTicks, periodTicks));
    }

    private PlatformRegistration register(BukkitTask task) {
        return new PlatformRegistration() {
            @Override
            public void unregister() {
                task.cancel();
            }

            @Override
            public boolean isRegistered() {
                var scheduler = BukkitTasks.this.plugin.getServer().getScheduler();
                return scheduler.isQueued(task.getTaskId()) || scheduler.isCurrentlyRunning(task.getTaskId());
            }
        };
    }

}
