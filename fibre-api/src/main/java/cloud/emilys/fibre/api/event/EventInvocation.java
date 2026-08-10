package cloud.emilys.fibre.api.event;

import java.lang.reflect.Method;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface EventInvocation {

    Method getListener();

    Event getEvent();

    boolean ignoresGlobal();
}
