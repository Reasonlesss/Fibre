package codes.reason.fibre.features.container.player;

import codes.reason.fibre.api.scope.ScopedObject;
import codes.reason.fibre.api.scope.lifecycle.ObjectInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PerPlayerContainerInitializer implements ObjectInitializer {

    @Override
    public void initialize(ScopedObject object) {
        if (object.getObject() instanceof PerPlayerContainer<?> container) {
            container.initialize(object.getScope());
        }
    }
}
