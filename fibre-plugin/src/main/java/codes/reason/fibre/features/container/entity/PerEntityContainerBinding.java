package codes.reason.fibre.features.container.entity;

import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.ScopeResolver;
import codes.reason.fibre.api.scope.ScopedObject;
import codes.reason.fibre.api.scope.binding.Binding;
import codes.reason.fibre.api.scope.binding.BindingResult;
import codes.reason.fibre.api.scope.binding.DependencySet;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PerEntityContainerBinding implements Binding {

    private final ObjectKey valueKey;

    public PerEntityContainerBinding(ObjectKey containerKey) {
        Type containerType = containerKey.type();
        if (!(containerType instanceof ParameterizedType parameterized)
                || parameterized.getActualTypeArguments().length != 1) {
            throw new IllegalArgumentException(
                    "Per-entity containers must declare one value type: %s".formatted(containerKey));
        }
        this.valueKey = ObjectKey.fromType(parameterized.getActualTypeArguments()[0]);
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create();
    }

    @Override
    public BindingResult make(ScopeResolver resolver) {
        return BindingResult.immediate(new PerEntityContainer<>(this.valueKey));
    }

    @Override
    public void destroy(ScopeResolver resolver, ScopedObject object) {
        if (object.getObject() instanceof PerEntityContainer<?> container) {
            container.close();
        }
    }
}
