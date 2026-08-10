package cloud.emilys.fibre.features.event;

import cloud.emilys.fibre.api.annotation.event.Listen;
import cloud.emilys.fibre.api.fact.FactScanContext;
import cloud.emilys.fibre.api.fact.FactScanner;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EventFactScanner implements FactScanner {

    @Override
    public void collect(FactScanContext context) {
        List<EventMethod> methods = new ArrayList<>();
        for (Method method : context.getMethods()) {
            if (!method.isAnnotationPresent(Listen.class)) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                context.report(method, "Event listeners must not be static.");
            }
            if (method.getReturnType() != void.class) {
                context.report(method, "Event listeners must return void.");
            }
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                context.report(method, "Event listeners must contain a single parameter that extends Event");
            }
            if (!context.isValid()) {
                continue;
            }
            Listen annotation = method.getAnnotation(Listen.class);
            EventPriority priority = annotation.priority();
            boolean ignoreGlobal = annotation.ignoreGlobal();
            method.setAccessible(true);
            methods.add(new EventMethod(method, priority, ignoreGlobal));
        }
        if (!context.isValid()) {
            return;
        }
        context.put(EventFacts.METHODS, methods);
    }
}
