package cloud.emilys.fibre.features.dependency;

import cloud.emilys.fibre.api.annotation.dependency.Expose;
import cloud.emilys.fibre.api.annotation.dependency.Provides;
import cloud.emilys.fibre.api.annotation.dependency.Use;
import cloud.emilys.fibre.api.fact.FactScanContext;
import cloud.emilys.fibre.api.fact.FactScanner;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencyFactScanner implements FactScanner {

    @Override
    public void collect(FactScanContext context) {
        this.collectUseConstructor(context);
        this.collectUseFields(context);
        this.collectExposeFields(context);
        this.collectProvidesMethods(context);
    }

    private void collectUseConstructor(FactScanContext context) {
        List<Constructor<?>> constructors = context.getConstructors().stream()
                .filter(constructor -> constructor.isAnnotationPresent(Use.class))
                .toList();
        if (constructors.size() > 1) {
            for (Constructor<?> constructor : constructors) {
                context.report(constructor, "Only one constructor may be annotated with @Use.");
            }
            return;
        }
        Constructor<?> constructor = constructors.isEmpty()
                ? context.getConstructors().stream()
                        .filter(candidate -> candidate.getParameterCount() == 0)
                        .findFirst()
                        .orElse(null)
                : constructors.getFirst();
        if (constructor != null) {
            constructor.setAccessible(true);
            context.put(DependencyFacts.CONSTRUCTOR, constructor);
        }
    }

    private void collectUseFields(FactScanContext context) {
        List<Field> fields = new ArrayList<>();
        for (Field field : context.getFields()) {
            if (!field.isAnnotationPresent(Use.class)) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                context.report(field, "Fields annotated with @Use must not be static.");
            }
            if (Modifier.isFinal(field.getModifiers())) {
                context.report(field, "Fields annotated with @Use must not be final.");
            }
            field.setAccessible(true);
            fields.add(field);
        }
        context.put(DependencyFacts.USE_FIELDS, fields);
    }

    private void collectExposeFields(FactScanContext context) {
        List<Field> fields = new ArrayList<>();
        for (Field field : context.getFields()) {
            if (!field.isAnnotationPresent(Expose.class)) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                context.report(field, "Fields annotated with @Expose must not be static.");
            }
            field.setAccessible(true);
            fields.add(field);
        }
        context.put(DependencyFacts.EXPOSE_FIELDS, fields);
    }

    private void collectProvidesMethods(FactScanContext context) {
        List<Method> staticMethods = new ArrayList<>();
        List<Method> instanceMethods = new ArrayList<>();
        for (Method method : context.getMethods()) {
            if (!method.isAnnotationPresent(Provides.class)) {
                continue;
            }
            if (method.getReturnType() == void.class) {
                context.report(method, "Methods annotated with @Provides must return a value.");
            }
            method.setAccessible(true);
            if (Modifier.isStatic(method.getModifiers())) {
                staticMethods.add(method);
            } else {
                instanceMethods.add(method);
            }
        }
        context.put(DependencyFacts.STATIC_PROVIDES_METHODS, staticMethods);
        context.put(DependencyFacts.INSTANCE_PROVIDES_METHODS, instanceMethods);
    }
}
