package cloud.emilys.fibre.features.event;

import java.lang.reflect.Method;
import org.bukkit.event.EventPriority;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record EventMethod(Method method, EventPriority priority, boolean ignoreGlobal) {}
