package cloud.emilys.fibre.api.scope.discovery;

import cloud.emilys.fibre.api.scope.ObjectKey;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopeContributor {

    void contribute(ScopeCollector collector, ObjectKey key);
}
