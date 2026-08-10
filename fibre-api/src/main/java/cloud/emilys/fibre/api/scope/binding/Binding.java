package cloud.emilys.fibre.api.scope.binding;

import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Binding {

    default BindingPriority getPriority() {
        return BindingPriority.NORMAL;
    }

    DependencySet getDependencies();

    BindingResult make(ScopeResolver resolver);

    default void destroy(ScopeResolver resolver, ScopedObject object) {}
}
