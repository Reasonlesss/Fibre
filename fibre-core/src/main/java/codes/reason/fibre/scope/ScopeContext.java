package codes.reason.fibre.scope;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@NullMarked
public abstract sealed class ScopeContext permits Scope, ScopeBuilder, ScopePluginBuilder {

    final @Nullable Scope parent;
    final Map<Identity<?>, Object> byIdentity;
    final Map<Class<?>, List<Object>> byType;

    ScopeContext(@Nullable Scope parent, Map<Identity<?>, Object> byIdentity, Map<Class<?>, List<Object>> byType) {
        this.parent = parent;
        this.byIdentity = byIdentity;
        this.byType = byType;
    }

    public final <T> Optional<T> get(Identity<T> identity) {
        Objects.requireNonNull(identity, "identity must not be null");
        Object value = this.byIdentity.get(identity);
        if (value != null) {
            return Optional.of(identity.cast(value));
        }
        return this.parent != null ? this.parent.get(identity) : Optional.empty();
    }

    public final <T> T require(Identity<T> identity) {
        return this.get(identity).orElseThrow(() -> new IllegalStateException(
                "No value found for %s in this scope or in parents"
                        .formatted(identity)
        ));
    }

    public final <T> List<T> list(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        List<T> result = new ArrayList<>();
        for (ScopeContext context = this; context != null; context = context.parent) {
            for (Object value : context.byType.getOrDefault(type, List.of())) {
                result.add(type.cast(value));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public final <T> List<T> localList(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        List<T> result = new ArrayList<>();
        for (Object value : this.byType.getOrDefault(type, List.of())) {
            result.add(type.cast(value));
        }
        return Collections.unmodifiableList(result);
    }

}
