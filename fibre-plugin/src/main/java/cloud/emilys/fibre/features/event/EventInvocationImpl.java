package cloud.emilys.fibre.features.event;

import cloud.emilys.fibre.api.event.EventInvocation;
import java.lang.reflect.Method;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record EventInvocationImpl(Method listener, Event event, boolean ignoresGlobal) implements EventInvocation {
    @Override
    public Method getListener() {
        return this.listener;
    }

    @Override
    public Event getEvent() {
        return this.event;
    }
}
