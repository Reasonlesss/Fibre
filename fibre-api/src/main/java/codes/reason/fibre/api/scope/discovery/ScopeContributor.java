package codes.reason.fibre.api.scope.discovery;

import codes.reason.fibre.api.scope.ObjectKey;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopeContributor {

    void contribute(ScopeCollector collector, ObjectKey key);
}
