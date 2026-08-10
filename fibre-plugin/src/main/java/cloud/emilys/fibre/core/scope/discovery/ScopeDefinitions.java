package cloud.emilys.fibre.core.scope.discovery;

import cloud.emilys.fibre.api.scope.ObjectKey;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ScopeDefinitions implements Iterable<ScopeDefinition> {

    private final Map<ObjectKey, ScopeDefinition> definitions = new LinkedHashMap<>();

    public void add(ScopeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        ScopeDefinition previous = this.definitions.putIfAbsent(definition.key(), definition);
        if (previous != null) {
            throw new IllegalArgumentException("Multiple definitions exist for %s".formatted(definition.key()));
        }
    }

    public ScopeDefinition require(ObjectKey key) {
        Objects.requireNonNull(key, "key");
        ScopeDefinition definition = this.definitions.get(key);
        if (definition == null) {
            throw new IllegalStateException("No definition exists for %s".formatted(key));
        }
        return definition;
    }

    @Override
    public Iterator<ScopeDefinition> iterator() {
        return Collections.unmodifiableCollection(this.definitions.values()).iterator();
    }
}
