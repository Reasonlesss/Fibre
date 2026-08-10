package cloud.emilys.fibre.features.container.dynamic;

import cloud.emilys.fibre.api.annotation.container.Initially;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DynamicContainerBinding implements Binding {

    private final ObjectKey initialKey;

    public DynamicContainerBinding(ObjectKey containerKey) {
        this.initialKey = resolveInitialKey(containerKey);
    }

    static ObjectKey resolveInitialKey(ObjectKey containerKey) {
        ObjectKey valueKey = getValueKey(containerKey);
        Class<?> valueClass = valueKey.getObjectClass();
        if (Modifier.isFinal(valueClass.getModifiers())) {
            throw new IllegalArgumentException(
                    "Dynamic value type must not be final: %s".formatted(valueClass.getTypeName()));
        }

        Initially initially = valueClass.getAnnotation(Initially.class);
        if (initially == null) {
            if (valueClass.isInterface() || Modifier.isAbstract(valueClass.getModifiers())) {
                throw new IllegalArgumentException(
                        "Abstract dynamic value types must declare @Initially: %s".formatted(containerKey));
            }
            return ObjectKey.fromNamedType(valueClass, valueKey.qualifier());
        }

        Class<?> initialClass = initially.value();
        if (!valueClass.isAssignableFrom(initialClass)) {
            throw new IllegalArgumentException("Dynamic initial type %s is not assignable to %s"
                    .formatted(initialClass.getTypeName(), valueClass.getTypeName()));
        }
        return ObjectKey.fromNamedType(initialClass, valueKey.qualifier());
    }

    private static ObjectKey getValueKey(ObjectKey containerKey) {
        if (!(containerKey.type() instanceof ParameterizedType parameterized)
                || parameterized.getActualTypeArguments().length != 1) {
            throw new IllegalArgumentException(
                    "Dynamic containers must declare one value type: %s".formatted(containerKey));
        }

        if (containerKey.getAnnotatedType().orElse(null) instanceof AnnotatedParameterizedType annotated) {
            AnnotatedType[] valueTypes = annotated.getAnnotatedActualTypeArguments();
            if (valueTypes.length == 1) {
                return ObjectKey.fromAnnotatedType(valueTypes[0]);
            }
        }
        return ObjectKey.fromType(parameterized.getActualTypeArguments()[0]);
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create();
    }

    @Override
    public BindingResult make(ScopeResolver resolver) {
        return BindingResult.immediate(new DynamicContainer<>(this.initialKey));
    }

    @Override
    public void destroy(ScopeResolver resolver, ScopedObject object) {
        if (object.getObject() instanceof DynamicContainer<?> container) {
            container.close();
        }
    }
}
