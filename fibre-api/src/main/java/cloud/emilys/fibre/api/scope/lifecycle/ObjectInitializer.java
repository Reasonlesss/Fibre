package cloud.emilys.fibre.api.scope.lifecycle;

import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ObjectInitializer {

    default DependencySet getDependencies() {
        return DependencySet.create();
    }

    void initialize(ScopeResolver resolver, ScopedObject object);
}
