package codes.reason.fibre.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;

// finds a value in an event from another plugin by its first no-args getter of that type
@NullMarked
public final class EventFields {

    private final List<String> platformPackages;
    private final Map<Key, Optional<Method>> getters = new HashMap<>();

    public EventFields(List<String> platformPackages) {
        this.platformPackages = List.copyOf(platformPackages);
    }

    public <T> Optional<T> get(Event event, Class<T> type) {
        if (this.isPlatformEvent(event)) {
            return Optional.empty();
        }
        return this.getters.computeIfAbsent(new Key(event.getClass(), type), key -> getter(key.event(), key.type()))
                .map(getter -> {
                    try {
                        return type.cast(getter.invoke(event));
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        throw new IllegalStateException("Unable to get %s from %s"
                                .formatted(type.getSimpleName(), event.getEventName()), exception);
                    }
                });
    }

    // where an event from another plugin happened, from a location, entity or block it has
    public Optional<Location> location(Event event) {
        return this.get(event, Location.class)
                .or(() -> this.get(event, Entity.class).map(Entity::getLocation))
                .or(() -> this.get(event, Block.class).map(Block::getLocation));
    }

    private boolean isPlatformEvent(Event event) {
        String packageName = event.getClass().getPackageName();
        return this.platformPackages.stream().anyMatch(packageName::startsWith);
    }

    private static Optional<Method> getter(Class<?> event, Class<?> type) {
        for (Method method : event.getMethods()) {
            if (method.getParameterCount() == 0 && type.isAssignableFrom(method.getReturnType())) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    private record Key(Class<?> event, Class<?> type) {}

}
