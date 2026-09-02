package codes.reason.fibre.features.container.dynamic;

import codes.reason.fibre.api.scope.ScopedObject;
import codes.reason.fibre.api.scope.lifecycle.ObjectInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DynamicContainerInitializer implements ObjectInitializer {

    @Override
    public void initialize(ScopedObject object) {
        if (object.getObject() instanceof DynamicContainer<?> container) {
            container.initialize(object.getScope());
        }
    }
}
