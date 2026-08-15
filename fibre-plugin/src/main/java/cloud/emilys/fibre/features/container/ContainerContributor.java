package cloud.emilys.fibre.features.container;

import cloud.emilys.fibre.api.container.Dynamic;
import cloud.emilys.fibre.api.container.PerEntity;
import cloud.emilys.fibre.api.container.PerPlayer;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.discovery.ScopeCollector;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import cloud.emilys.fibre.features.container.dynamic.DynamicContainer;
import cloud.emilys.fibre.features.container.dynamic.DynamicContainerBinding;
import cloud.emilys.fibre.features.container.dynamic.DynamicContainerInitializer;
import cloud.emilys.fibre.features.container.entity.PerEntityContainer;
import cloud.emilys.fibre.features.container.entity.PerEntityContainerBinding;
import cloud.emilys.fibre.features.container.entity.PerEntityContainerInitializer;
import cloud.emilys.fibre.features.container.player.PerPlayerContainer;
import cloud.emilys.fibre.features.container.player.PerPlayerContainerBinding;
import cloud.emilys.fibre.features.container.player.PerPlayerContainerInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ContainerContributor implements ScopeContributor {

    @Override
    public void contribute(ScopeCollector collector, ObjectKey key) {
        if (key.getObjectClass() == Dynamic.class) {
            collector.bind(key, new DynamicContainerBinding(key));
            collector.initialize(key, new DynamicContainerInitializer());
            collector.activate(key, (_, object) -> ((DynamicContainer<?>) object.getObject()).activate());
            return;
        }
        if (key.getObjectClass() == PerPlayer.class) {
            collector.bind(key, new PerPlayerContainerBinding(key));
            collector.initialize(key, new PerPlayerContainerInitializer());
            collector.activate(key, (_, object) -> ((PerPlayerContainer<?>) object.getObject()).activate());
            return;
        }
        if (key.getObjectClass() == PerEntity.class) {
            collector.bind(key, new PerEntityContainerBinding(key));
            collector.initialize(key, new PerEntityContainerInitializer());
            collector.activate(key, (_, object) -> ((PerEntityContainer<?>) object.getObject()).activate());
        }
    }
}
