package cloud.emilys.fibre.features.state;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.discovery.ScopeCollector;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StateContributor implements ScopeContributor {

    @Override
    public void contribute(ScopeCollector collector, ObjectKey key) {
        if (!collector.getFactsFor(key).has(StateFacts.STATE_MACHINE_INITIAL_STATE)) {
            return;
        }
        collector.initialize(key, (_, object) -> {
            Class<?> initialState = object.getFacts().require(StateFacts.STATE_MACHINE_INITIAL_STATE);
            StateManager manager = new StateManager(object.getScope(), initialState);
            object.track(manager);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        manager.tick();
                    } catch (RuntimeException exception) {
                        object.getScope().getGame().handleException(exception);
                    }
                }
            }.runTaskTimer(object.getPlugin(), 0L, 1L);
            object.track(task::cancel);
        });
    }
}
