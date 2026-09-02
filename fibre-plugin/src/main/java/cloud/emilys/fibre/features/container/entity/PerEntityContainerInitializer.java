package cloud.emilys.fibre.features.container.entity;

import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PerEntityContainerInitializer implements ObjectInitializer {

    @Override
    public void initialize(ScopedObject object) {
        if (object.getObject() instanceof PerEntityContainer<?> container) {
            container.initialize(object.getScope());
        }
    }
}
