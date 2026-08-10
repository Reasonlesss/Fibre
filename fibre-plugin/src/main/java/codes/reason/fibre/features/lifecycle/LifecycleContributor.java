package codes.reason.fibre.features.lifecycle;

import codes.reason.fibre.FibrePlugin;
import codes.reason.fibre.api.fact.FactKey;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.ScopedObject;
import codes.reason.fibre.api.scope.discovery.ScopeCollector;
import codes.reason.fibre.api.scope.discovery.ScopeContributor;
import codes.reason.fibre.core.util.ExceptionUtil;
import java.lang.reflect.Method;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LifecycleContributor implements ScopeContributor {

    @Override
    public void contribute(ScopeCollector collector, ObjectKey key) {
        collector.initialize(key, (_, object) -> {
            dispatchLifecycleMethods(object, LifecycleFacts.CONFIGURE);
        });
        collector.postInitialize(key, (_, object) -> {
            dispatchLifecycleMethods(object, LifecycleFacts.ENTER);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    dispatchLifecycleMethods(object, LifecycleFacts.TICK);
                }
            }.runTaskTimer(FibrePlugin.getPlugin(FibrePlugin.class), 0L, 1L);
            object.track(task::cancel);
            object.track(() -> dispatchLifecycleMethods(object, LifecycleFacts.EXIT));
        });
    }

    private static void dispatchLifecycleMethods(ScopedObject object, FactKey<List<Method>> type) {
        object.getFacts().get(type).ifPresent(methods -> {
            for (Method method : methods) {
                try {
                    method.invoke(object.getObject());
                } catch (Throwable failure) {
                    object.getGame()
                            .handleException(new RuntimeException(
                                    "Encountered an exception while calling lifecycle method %s"
                                            .formatted(method.toGenericString()),
                                    ExceptionUtil.unwrap(failure)));
                    return;
                }
            }
        });
    }
}
