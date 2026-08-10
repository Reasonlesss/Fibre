package cloud.emilys.fibre.features.event;

import cloud.emilys.fibre.api.event.EventInvocation;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.discovery.ScopeCollector;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EventContributor implements ScopeContributor {

    @SuppressWarnings("unchecked")
    @Override
    public void contribute(ScopeCollector collector, ObjectKey key) {
        collector.initialize(key, (resolver, object) -> {
            Optional<List<EventMethod>> methods = object.getFacts().get(EventFacts.METHODS);
            if (methods.isEmpty()) {
                return;
            }
            Listener listener = new Listener() {};
            for (EventMethod method : methods.get()) {
                EventPriority priority = method.priority();
                Class<?> eventType = method.method().getParameterTypes()[0];

                Bukkit.getServer()
                        .getPluginManager()
                        .registerEvent(
                                (Class<? extends Event>) eventType,
                                listener,
                                priority,
                                (_, event) -> {
                                    try {
                                        EventInvocation invocation =
                                                new EventInvocationImpl(method.method(), event, method.ignoreGlobal());
                                        if (eventType.isInstance(event)
                                                && object.getScope().accepts(event)
                                                && object.getScope().accepts(invocation)) {
                                            method.method().setAccessible(true);
                                            method.method().invoke(object.getObject(), event);
                                        }
                                    } catch (Exception exception) {
                                        object.getScope().getGame().handleException(exception);
                                    }
                                },
                                object.getPlugin());
            }
            object.track(() -> HandlerList.unregisterAll(listener));
        });
    }
}
