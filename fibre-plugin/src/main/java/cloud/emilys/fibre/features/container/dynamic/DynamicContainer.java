package cloud.emilys.fibre.features.container.dynamic;

import cloud.emilys.fibre.api.container.Dynamic;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class DynamicContainer<T> implements Dynamic<T>, AutoCloseable {

    private final ObjectKey initialKey;
    private @Nullable Scope parent;
    private @Nullable Scope currentScope;
    private @Nullable ObjectKey currentKey;
    private boolean active;

    public DynamicContainer(ObjectKey initialKey) {
        this.initialKey = initialKey;
    }

    public void initialize(Scope parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Dynamic container is already initialized");
        }
        this.parent = parent;
    }

    public void activate() {
        if (this.parent == null) {
            throw new IllegalStateException("Dynamic container is not initialized");
        }
        if (this.active) {
            throw new IllegalStateException("Dynamic container is already active");
        }
        this.active = true;
        this.set(this.initialKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        Scope scope = this.currentScope;
        ObjectKey key = this.currentKey;
        if (!this.active) {
            throw new IllegalStateException("Dynamic container is not active during scope configuration");
        }
        if (scope == null || key == null) {
            throw new IllegalStateException("Dynamic container has no current value");
        }
        return (T) scope.require(key).getObject();
    }

    @Override
    public void set(Class<? extends T> type) {
        if (!this.active) {
            throw new IllegalStateException("Dynamic container is not active during scope configuration");
        }
        this.set(ObjectKey.fromType(type));
    }

    private void set(ObjectKey key) {
        Scope parent = this.parent;
        if (parent == null) {
            throw new IllegalStateException("Dynamic container is not initialized");
        }
        if (this.currentScope != null) {
            this.currentScope.close();
        }
        this.currentScope = ScopeFactory.synchronous()
                .initialize(ScopeBlueprint.builder()
                        .setParent(parent)
                        .setGame(parent.getGame())
                        .setInitialKey(key)
                        .build());
        this.currentScope.setName("Dynamic<" + key.getObjectClass().getSimpleName() + ">");
        this.currentKey = key;
    }

    @Override
    public void close() {
        if (this.currentScope != null) {
            this.currentScope.close();
        }
        this.currentScope = null;
        this.currentKey = null;
        this.active = false;
        this.parent = null;
    }
}
