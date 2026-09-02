package codes.reason.fibre.api.scope.lifecycle;

import codes.reason.fibre.api.scope.ScopedObject;
import codes.reason.fibre.api.scope.binding.DependencySet;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ObjectInitializer {

    default DependencySet getDependencies() {
        return DependencySet.create();
    }

    void initialize(ScopedObject object);
}
