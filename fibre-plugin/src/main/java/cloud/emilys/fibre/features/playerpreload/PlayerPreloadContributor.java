package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.playerpreload.PlayerPreloadException;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.discovery.ScopeCollector;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import java.util.ArrayList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadContributor implements ScopeContributor {

    @Override
    public void contribute(ScopeCollector collector, ObjectKey key) {
        if (!collector.getFactsFor(key).has(PlayerPreloadFacts.METHODS)) {
            return;
        }
        collector.initialize(key, object -> {
            if (!object.getScope().getParents().isEmpty()) {
                throw new PlayerPreloadException("@PlayerPreload annotated method is present outside the root scope.");
            }
            object.getScope()
                    .getOrPut(PlayerPreloadDataKeys.METHODS, ArrayList::new)
                    .addAll(object.getFacts().require(PlayerPreloadFacts.METHODS));
        });
    }
}
