package cloud.emilys.fibre.features.dependency;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.binding.BindingPriority;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import java.lang.reflect.Field;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ExposeBinding implements Binding {

    private final ObjectKey instance;
    private final Field field;

    public ExposeBinding(ObjectKey instance, Field field) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.field = Objects.requireNonNull(field, "field");
    }

    @Override
    public BindingPriority getPriority() {
        return BindingPriority.HIGHEST;
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create().add(this.instance);
    }

    @Override
    public BindingResult make(ScopeResolver resolver) {
        Objects.requireNonNull(resolver, "resolver");
        Object object = resolver.require(this.instance).getObject();
        try {
            return BindingResult.immediate(this.field.get(object));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot expose dependency field %s".formatted(this.field.toGenericString()), exception);
        }
    }
}
