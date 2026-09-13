package cloud.emilys.fibre.features.container.player;

import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
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
