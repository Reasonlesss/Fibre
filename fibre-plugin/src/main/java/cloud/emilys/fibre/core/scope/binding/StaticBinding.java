package cloud.emilys.fibre.core.scope.binding;

import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.binding.BindingPriority;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StaticBinding implements Binding {

    private final Object object;

    public StaticBinding(Object object) {
        this.object = object;
    }

    @Override
    public BindingPriority getPriority() {
        return BindingPriority.HIGHEST;
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create();
    }

    @Override
    public BindingResult make(ScopeResolver resolver) {
        return BindingResult.immediate(this.object);
    }
}
