package cloud.emilys.fibre.features.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EventFieldAccessor {

    private final Map<EventFieldSignature, Optional<Method>> cachedEventFields = new ConcurrentHashMap<>();

    public Optional<Method> getMethod(Event event, Class<?> type) {
        EventFieldSignature signature = new EventFieldSignature(type, event.getClass());
        return this.cachedEventFields.computeIfAbsent(signature, ignored -> {
            for (Method method : event.getClass().getMethods()) {
                if (method.getParameterCount() != 0) {
                    continue;
                }
                if (!type.isAssignableFrom(method.getReturnType())) {
                    continue;
                }
                return Optional.of(method);
            }
            return Optional.empty();
        });
    }

    public <T> Optional<T> get(Event event, Class<T> type) {
        return this.getMethod(event, type).map(method -> {
            try {
                return type.cast(method.invoke(event));
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new IllegalStateException(
                        "Unable to obtain %s from %s".formatted(type.getSimpleName(), event.getEventName()), exception);
            }
        });
    }

    @NullMarked
    private record EventFieldSignature(Class<?> field, Class<?> event) {}
}
