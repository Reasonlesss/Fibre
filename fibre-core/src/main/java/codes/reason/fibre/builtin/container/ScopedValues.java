package codes.reason.fibre.builtin.container;

import codes.reason.fibre.scope.Handle;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.ScopeOptions;
import codes.reason.fibre.scope.identity.Identity;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

// one child scope of the owner per id, each holding a T
@NullMarked
final class ScopedValues<T> {

    private final Handle<Scope> owner;
    private final Class<?> type;
    private final Map<UUID, Scope> scopes = new LinkedHashMap<>();

    ScopedValues(Identity<?> identity, Handle<Scope> owner) {
        this.owner = owner;
        Type argument = ((ParameterizedType) identity.type()).getActualTypeArguments()[0];
        this.type = argument instanceof ParameterizedType parameterized
                ? (Class<?>) parameterized.getRawType()
                : (Class<?>) argument;
    }

    void add(UUID id, Consumer<? super ScopeOptions> options) {
        if (this.scopes.containsKey(id)) {
            return;
        }
        Scope scope = this.owner.require().child(this.type, options);
        if (!scope.isClosed()) {
            this.scopes.put(id, scope);
        }
    }

    void remove(UUID id) {
        Scope scope = this.scopes.remove(id);
        if (scope != null) {
            scope.close();
        }
    }

    @SuppressWarnings("unchecked")
    Optional<T> get(UUID id) {
        Scope scope = this.scopes.get(id);
        return scope != null ? Optional.of((T) scope.require(this.type)) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    List<T> all() {
        List<T> values = new ArrayList<>();
        for (Scope scope : this.scopes.values()) {
            values.add((T) scope.require(this.type));
        }
        return values;
    }

    void clear() {
        List<Scope> scopes = List.copyOf(this.scopes.values());
        this.scopes.clear();
        for (Scope scope : scopes.reversed()) {
            scope.close();
        }
    }

    String typeName() {
        return this.type.getSimpleName();
    }

}
