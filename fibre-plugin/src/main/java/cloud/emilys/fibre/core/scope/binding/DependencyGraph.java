package cloud.emilys.fibre.core.scope.binding;

import cloud.emilys.fibre.api.scope.DependencyCycleException;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencyGraph implements Iterable<ObjectKey> {

    private final Map<ObjectKey, Set<ObjectKey>> dependencies = new LinkedHashMap<>();
    private final Map<ObjectKey, Set<ObjectKey>> dependents = new LinkedHashMap<>();

    public void addDependencies(ObjectKey key, DependencySet dependencies) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(dependencies, "dependencies");

        this.ensureExists(key);
        Set<ObjectKey> objectDependencies = this.dependencies.get(key);
        for (ObjectKey dependency : dependencies.asSet()) {
            this.ensureExists(dependency);
            Set<ObjectKey> dependents = this.dependents.get(dependency);

            objectDependencies.add(dependency);
            dependents.add(key);
        }
    }

    private void ensureExists(ObjectKey key) {
        this.dependencies.computeIfAbsent(key, _ -> new HashSet<>());
        this.dependents.computeIfAbsent(key, _ -> new HashSet<>());
    }

    public List<ObjectKey> sort() {
        List<ObjectKey> sorted = new ArrayList<>();
        Map<ObjectKey, Integer> incoming = new HashMap<>();
        Deque<ObjectKey> toVisit = new ArrayDeque<>();

        for (Map.Entry<ObjectKey, Set<ObjectKey>> entry : this.dependencies.entrySet()) {
            incoming.put(entry.getKey(), entry.getValue().size());
            if (entry.getValue().isEmpty()) {
                toVisit.addLast(entry.getKey());
            }
        }

        while (!toVisit.isEmpty()) {
            ObjectKey current = toVisit.removeFirst();
            sorted.add(current);
            for (ObjectKey dependent : this.dependents.get(current)) {
                int remainingDependents = incoming.compute(dependent, (_, count) -> Objects.requireNonNull(count) - 1);
                if (remainingDependents == 0) {
                    incoming.remove(dependent);
                    toVisit.addLast(dependent);
                }
            }
        }

        if (sorted.size() != this.dependencies.size()) {
            String unresolved = incoming.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .map(ObjectKey::toString)
                    .collect(Collectors.joining("\n - ", "\n - ", ""));

            throw new DependencyCycleException("Dependency cycle detected involving: %s".formatted(unresolved));
        }

        return sorted;
    }

    @Override
    public Iterator<ObjectKey> iterator() {
        return this.sort().iterator();
    }
}
