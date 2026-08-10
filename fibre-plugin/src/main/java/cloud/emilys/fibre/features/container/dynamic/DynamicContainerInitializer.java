package cloud.emilys.fibre.features.container.dynamic;

import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DynamicContainerInitializer implements ObjectInitializer {

    @Override
    public void initialize(ScopeResolver resolver, ScopedObject object) {
        if (object.getObject() instanceof DynamicContainer<?> container) {
            container.initialize(object.getScope());
        }
    }
}
