package cloud.emilys.fibre.api.scope.binding;

import cloud.emilys.fibre.api.FibreBridge;
import cloud.emilys.fibre.api.scope.ObjectKey;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface DependencySet {

    static DependencySet create() {
        return FibreBridge.get().createDependencySet();
    }

    DependencySet add(ObjectKey objectId);

    default DependencySet add(Collection<ObjectKey> objectIds) {
        Objects.requireNonNull(objectIds, "objectIds");
        objectIds.forEach(this::add);
        return this;
    }

    default DependencySet add(ObjectKey... objectIds) {
        Objects.requireNonNull(objectIds, "objectIds");
        for (ObjectKey objectId : objectIds) {
            this.add(objectId);
        }
        return this;
    }

    default DependencySet add(DependencySet other) {
        Objects.requireNonNull(other, "other");
        return this.add(other.asSet());
    }

    Set<ObjectKey> asSet();
}
