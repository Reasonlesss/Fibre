package cloud.emilys.fibre.core.scope.discovery;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.fact.Facts;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.discovery.ScopeCollector;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
import cloud.emilys.fibre.core.FibreImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ScopeCollectorImpl implements ScopeCollector {

    private final FibreImpl api;
    private final Map<ObjectKey, Binding> bindings = new LinkedHashMap<>();
    private final Map<ObjectKey, List<ObjectInitializer>> initializers = new LinkedHashMap<>();
    private final Map<ObjectKey, List<ObjectInitializer>> postInitializers = new LinkedHashMap<>();
    private final Set<ObjectKey> visited = new HashSet<>();

    public ScopeCollectorImpl() {
        this.api = (FibreImpl) Fibre.get();
    }

    @Override
    public Facts getFactsFor(ObjectKey object) {
        Objects.requireNonNull(object, "object");
        return this.api.getClassFactIndex().get(object.getObjectClass());
    }

    @Override
    public void bind(ObjectKey object, Binding binding) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(binding, "binding");
        this.bindings.merge(object, binding, (current, candidate) -> {
            if (candidate.getPriority().isGreaterThan(current.getPriority())) {
                return candidate;
            }
            return current;
        });
    }

    @Override
    public void initialize(ObjectKey object, ObjectInitializer initializer) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(initializer, "initializer");
        this.initializers.computeIfAbsent(object, _ -> new ArrayList<>()).add(initializer);
    }

    @Override
    public void postInitialize(ObjectKey object, ObjectInitializer initializer) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(initializer, "initializer");
        this.postInitializers.computeIfAbsent(object, _ -> new ArrayList<>()).add(initializer);
    }

    @Override
    public void visit(ObjectKey object) {
        Objects.requireNonNull(object, "object");
        if (!this.visited.add(object)) {
            return;
        }
        for (ScopeContributor contributor : this.api.getScopeContributors()) {
            contributor.contribute(this, object);
        }
    }

    public ScopeDefinitions collect() {
        ScopeDefinitions objects = new ScopeDefinitions();
        for (Map.Entry<ObjectKey, Binding> entry : this.bindings.entrySet()) {
            objects.add(new ScopeDefinition(
                    entry.getKey(),
                    this.getFactsFor(entry.getKey()),
                    entry.getValue(),
                    List.copyOf(this.initializers.getOrDefault(entry.getKey(), List.of())),
                    List.copyOf(this.postInitializers.getOrDefault(entry.getKey(), List.of()))));
        }
        return objects;
    }
}
