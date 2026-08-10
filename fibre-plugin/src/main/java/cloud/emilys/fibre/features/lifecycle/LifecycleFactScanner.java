package cloud.emilys.fibre.features.lifecycle;

import cloud.emilys.fibre.api.annotation.lifecycle.Configure;
import cloud.emilys.fibre.api.annotation.lifecycle.Enter;
import cloud.emilys.fibre.api.annotation.lifecycle.Exit;
import cloud.emilys.fibre.api.annotation.lifecycle.Tick;
import cloud.emilys.fibre.api.fact.FactKey;
import cloud.emilys.fibre.api.fact.FactScanContext;
import cloud.emilys.fibre.api.fact.FactScanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LifecycleFactScanner implements FactScanner {

    @Override
    public void collect(FactScanContext context) {
        this.collectMethods(context, Configure.class, LifecycleFacts.CONFIGURE);
        this.collectMethods(context, Enter.class, LifecycleFacts.ENTER);
        this.collectMethods(context, Tick.class, LifecycleFacts.TICK);
        this.collectMethods(context, Exit.class, LifecycleFacts.EXIT);
    }

    private void collectMethods(
            FactScanContext context, Class<? extends Annotation> annotation, FactKey<List<Method>> key) {
        List<Method> methods = new ArrayList<>();
        for (Method method : context.getMethods()) {
            if (!method.isAnnotationPresent(annotation)) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                context.report(method, "Lifecycle methods must not be static.");
            }
            if (method.getParameterCount() != 0) {
                context.report(method, "Lifecycle methods must not contain any parameters.");
            }
            if (method.getReturnType() != void.class) {
                context.report(method, "Lifecycle methods must return void.");
            }
            method.setAccessible(true);
            methods.add(method);
        }
        if (methods.isEmpty()) {
            return;
        }
        context.put(key, methods);
    }
}
