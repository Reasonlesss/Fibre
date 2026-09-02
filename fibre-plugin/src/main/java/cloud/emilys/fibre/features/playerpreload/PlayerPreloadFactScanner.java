package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.annotation.dependency.PlayerPreload;
import cloud.emilys.fibre.api.fact.FactScanContext;
import cloud.emilys.fibre.api.fact.FactScanner;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadFactScanner implements FactScanner {

    @Override
    public void collect(FactScanContext context) {
        List<Method> methods = new ArrayList<>();
        for (Method method : context.getMethods()) {
            if (!method.isAnnotationPresent(PlayerPreload.class)) {
                continue;
            }
            if (!CompletionStage.class.isAssignableFrom(method.getReturnType())) {
                context.report(method, "Methods annotated with @PlayerPreload must return a CompletionStage.");
            } else {
                try {
                    if (PlayerPreloadUtil.getPreloadedKey(method).getObjectClass() == Void.class) {
                        context.report(method, "Methods annotated with @PlayerPreload must produce a value.");
                    }
                } catch (IllegalArgumentException _) {
                    context.report(method, "Methods annotated with @PlayerPreload must declare one result type.");
                }
            }
            if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != UUID.class) {
                context.report(method, "Methods annotated with @PlayerPreload must accept one UUID parameter.");
            }
            method.setAccessible(true);
            if (!Modifier.isStatic(method.getModifiers())) {
                context.report(method, "Methods annotated with @PlayerPreload must be static.");
            }
            methods.add(method);
        }
        context.put(PlayerPreloadFacts.METHODS, methods);
    }
}
