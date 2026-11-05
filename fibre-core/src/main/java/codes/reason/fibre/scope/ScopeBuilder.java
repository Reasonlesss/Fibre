package codes.reason.fibre.scope;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@NullMarked
public final class ScopeBuilder extends ScopeContext {

    private boolean done;

    ScopeBuilder(@Nullable Scope parent) {
        super(parent, new HashMap<>(), Map.of());
    }

    public <T> ScopeBuilder bind(Identity<T> identity, T value) {
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.ensureNotDone();
        Object existing = this.byIdentity.putIfAbsent(identity, identity.cast(value));
        if (existing != null) {
            throw new IllegalStateException("%s is already bound in this scope".formatted(identity));
        }
        return this;
    }

    public ScopePluginBuilder plugins() {
        this.ensureNotDone();
        this.done = true;
        return new ScopePluginBuilder(this.parent, Map.copyOf(this.byIdentity));
    }

    private void ensureNotDone() {
        if (this.done) {
            throw new IllegalStateException("Binding stage has already ended");
        }
    }

}
