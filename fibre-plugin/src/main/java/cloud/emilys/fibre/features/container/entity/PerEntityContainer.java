package cloud.emilys.fibre.features.container.entity;

import cloud.emilys.fibre.api.container.PerEntity;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import cloud.emilys.fibre.core.util.ResourceCleanup;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class PerEntityContainer<T> implements PerEntity<T>, AutoCloseable {

    private final ObjectKey valueKey;
    private final Map<UUID, Scope> scopes = new LinkedHashMap<>();
    private @Nullable Scope parent;

    public PerEntityContainer(ObjectKey valueKey) {
        this.valueKey = valueKey;
    }

    public void initialize(Scope parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Per-entity container is already initialized");
        }
        this.parent = parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(Entity entity) {
        Scope scope = this.scopes.get(entity.getUniqueId());
        if (scope == null) {
            throw new IllegalArgumentException("Entity is not tracked by this container");
        }
        return (T) scope.require(this.valueKey).getObject();
    }

    @Override
    public void track(Entity entity) {
        UUID entityId = entity.getUniqueId();
        if (this.scopes.containsKey(entityId)) {
            return;
        }
        Scope parent = this.parent;
        if (parent == null) {
            throw new IllegalStateException("Per-entity container is not initialized");
        }
        Scope scope = ScopeFactory.synchronous()
                .initialize(ScopeBlueprint.builder()
                        .setParent(parent)
                        .setGame(parent.getGame())
                        .setInitialKey(this.valueKey)
                        .setInputObject(Entity.class, entity)
                        .build());
        scope.setName("PerEntity<" + this.valueKey.getObjectClass().getSimpleName() + "> " + entityId);
        scope.addFilter(Entity.class, candidate -> candidate.getUniqueId().equals(entityId));
        this.scopes.put(entityId, scope);
    }

    @Override
    public void untrack(Entity entity) {
        UUID entityId = entity.getUniqueId();
        Scope scope = this.scopes.get(entityId);
        if (scope == null) {
            return;
        }
        this.scopes.remove(entityId).close();
    }

    @Override
    public void close() {
        ResourceCleanup.closeAllQuietly(List.copyOf(this.scopes.values()));
        this.scopes.clear();
        this.parent = null;
    }
}
