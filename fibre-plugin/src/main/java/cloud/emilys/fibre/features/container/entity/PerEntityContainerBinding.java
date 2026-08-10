package cloud.emilys.fibre.features.container.entity;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
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
