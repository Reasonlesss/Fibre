package codes.reason.fibre.features.container;

import codes.reason.fibre.api.container.Dynamic;
import codes.reason.fibre.api.container.PerEntity;
import codes.reason.fibre.api.container.PerPlayer;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.discovery.ScopeCollector;
import codes.reason.fibre.api.scope.discovery.ScopeContributor;
import codes.reason.fibre.features.container.dynamic.DynamicContainerBinding;
import codes.reason.fibre.features.container.dynamic.DynamicContainerInitializer;
import codes.reason.fibre.features.container.entity.PerEntityContainerBinding;
import codes.reason.fibre.features.container.entity.PerEntityContainerInitializer;
import codes.reason.fibre.features.container.player.PerPlayerContainerBinding;
import codes.reason.fibre.features.container.player.PerPlayerContainerInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ContainerContributor implements ScopeContributor {

    @Override
    public void contribute(ScopeCollector collector, ObjectKey key) {
        if (key.getObjectClass() == Dynamic.class) {
            collector.bind(key, new DynamicContainerBinding(key));
            collector.initialize(key, new DynamicContainerInitializer());
            return;
        }
        if (key.getObjectClass() == PerPlayer.class) {
            collector.bind(key, new PerPlayerContainerBinding(key));
            collector.initialize(key, new PerPlayerContainerInitializer());
            return;
        }
        if (key.getObjectClass() == PerEntity.class) {
            collector.bind(key, new PerEntityContainerBinding(key));
            collector.initialize(key, new PerEntityContainerInitializer());
        }
    }
}
