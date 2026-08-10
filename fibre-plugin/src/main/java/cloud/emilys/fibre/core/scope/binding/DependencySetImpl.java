package cloud.emilys.fibre.core.scope.binding;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencySetImpl implements DependencySet {

    private final Set<ObjectKey> dependencies = new LinkedHashSet<>();

    @Override
    public DependencySet add(ObjectKey objectId) {
        Objects.requireNonNull(objectId, "objectId");
        this.dependencies.add(objectId);
        return this;
    }

    @Override
    public Set<ObjectKey> asSet() {
        return Set.copyOf(this.dependencies);
    }
}
