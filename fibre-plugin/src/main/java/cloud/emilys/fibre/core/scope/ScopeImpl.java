package cloud.emilys.fibre.core.scope;

import cloud.emilys.fibre.api.data.RuntimeData;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.scope.Filter;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.core.data.RuntimeDataImpl;
import cloud.emilys.fibre.core.util.ClassHierarchy;
import cloud.emilys.fibre.core.util.ResourceCleanup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ScopeImpl implements Scope {

    private final RuntimeData data = new RuntimeDataImpl();
    private final Map<Class<?>, List<Filter<?>>> filters = new LinkedHashMap<>();
    private final Map<ObjectKey, ScopedObject> objectMap;
    private final List<Scope> children = new ArrayList<>();
    private String name = "Scope";
    private final Game game;
    private @Nullable Scope parent;
    private boolean closed;

    public ScopeImpl(Game game, Map<ObjectKey, ? extends ScopedObject> objects) {
        this.game = Objects.requireNonNull(game, "game");
        this.objectMap = new LinkedHashMap<>(Objects.requireNonNull(objects, "objects"));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public @Nullable Scope getParent() {
        return this.parent;
    }

    @Override
    public List<Scope> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public void bindParent(Scope parent) {
        Objects.requireNonNull(parent, "parent");
        if (!(parent instanceof ScopeImpl parentScope)) {
            throw new IllegalArgumentException("Parent scope must be managed by Fibre");
        }
        if (this.parent != null) {
            throw new IllegalStateException("Scope already has a parent");
        }
        if (parent.getGame() != this.game) {
            throw new IllegalArgumentException("A child scope must belong to the same game as its parent");
        }
        Scope ancestor = parent;
        while (ancestor != null) {
            if (ancestor == this) {
                throw new IllegalArgumentException("Binding this parent would create a scope cycle");
            }
            ancestor = ancestor.getParent();
        }
        this.parent = parent;
        parentScope.children.add(this);
    }

    @Override
    public <T> void addFilter(Class<T> type, Filter<? super T> filter) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(filter, "filter");
        this.filters.computeIfAbsent(type, _ -> new ArrayList<>()).add(filter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean accepts(Object value) {
        Objects.requireNonNull(value, "value");
        Scope parent = this.parent;
        if (parent != null && !parent.accepts(value)) {
            return false;
        }
        for (Class<?> type : ClassHierarchy.getAllClasses(value)) {
            List<Filter<?>> filters = this.filters.get(type);
            if (filters != null) {
                for (Filter<?> filter : filters) {
                    if (!((Filter<Object>) filter).allows(value)) {
                        return false;
                    }
                }
            }
        }
        List<Filter<?>> objectFilters = this.filters.get(Object.class);
        if (value.getClass() != Object.class && objectFilters != null) {
            for (Filter<?> filter : objectFilters) {
                if (!((Filter<Object>) filter).allows(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Optional<ScopedObject> get(ObjectKey key) {
        Objects.requireNonNull(key, "key");
        ScopedObject object = this.objectMap.get(key);
        if (object != null) {
            return Optional.of(object);
        }
        Scope parent = this.parent;
        return parent == null ? Optional.empty() : parent.get(key);
    }

    @Override
    public List<ScopedObject> getLocalObjects() {
        return List.copyOf(this.objectMap.values());
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        ResourceCleanup.closeAllQuietly(List.copyOf(this.children));
        ResourceCleanup.closeAllQuietly(List.copyOf(this.objectMap.values()));
        if (this.parent instanceof ScopeImpl impl) {
            impl.children.remove(this);
        }
    }

    @Override
    public RuntimeData getRuntimeData() {
        return this.data;
    }
}
