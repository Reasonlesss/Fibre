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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ScopeImpl implements Scope {

    private final RuntimeData data = new RuntimeDataImpl();
    private final Map<Class<?>, List<Filter<?>>> filters = new LinkedHashMap<>();
    private final Map<ObjectKey, ScopedObject> objectMap;
    private final List<Scope> children = new ArrayList<>();
    private final List<Scope> parents = new ArrayList<>();
    private String name = "Scope";
    private final Game game;
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
    public List<Scope> getParents() {
        return Collections.unmodifiableList(this.parents);
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
        if (parent.getGame() != this.game) {
            throw new IllegalArgumentException("A child scope must belong to the same game as its parent");
        }
        if (!this.parents.isEmpty()) {
            Scope expectedRoot = rootOf(this);
            if (rootOf(parent) != expectedRoot) {
                throw new IllegalArgumentException(
                        "All parents must share the same root scope (expected %s)".formatted(expectedRoot.getName()));
            }
        }
        Deque<Scope> stack = new ArrayDeque<>();
        Set<Scope> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        stack.push(parent);
        while (!stack.isEmpty()) {
            Scope current = stack.pop();
            if (current == this) {
                throw new IllegalArgumentException("Binding this parent would create a scope cycle");
            }
            for (Scope ancestor : current.getParents()) {
                if (visited.add(ancestor)) {
                    stack.push(ancestor);
                }
            }
        }
        if (this.parents.add(parent)) {
            parentScope.children.add(this);
        }
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
        for (Scope parent : this.parents) {
            if (!parent.accepts(value)) {
                return false;
            }
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
        for (Scope parent : this.parents) {
            Optional<ScopedObject> found = parent.get(key);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
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
        for (Scope parent : this.parents) {
            if (parent instanceof ScopeImpl impl) {
                impl.children.remove(this);
            }
        }
    }

    private static Scope rootOf(Scope scope) {
        Scope current = scope;
        while (!current.getParents().isEmpty()) {
            current = current.getParents().getFirst();
        }
        return current;
    }

    @Override
    public RuntimeData getRuntimeData() {
        return this.data;
    }
}
