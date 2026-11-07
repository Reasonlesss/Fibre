package codes.reason.fibre.scope;

import codes.reason.fibre.identity.ClassIdentity;
import codes.reason.fibre.identity.Identity;
import codes.reason.fibre.plugin.ScopePlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ScopeOptions {

    private final Map<Identity<?>, Object> bindings = new LinkedHashMap<>();
    private final List<ScopePlugin> plugins = new ArrayList<>();
    private boolean done;

    ScopeOptions() {}

    public <T> ScopeOptions bind(Class<T> type, T value) {
        Objects.requireNonNull(type, "type must not be null");
        return this.bind(new ClassIdentity<>(type), value);
    }

    public <T> ScopeOptions bind(Identity<T> identity, T value) {
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.ensureNotDone();
        Object existing = this.bindings.putIfAbsent(identity, identity.cast(value));
        if (existing != null) {
            throw new IllegalStateException("%s is already bound in this scope".formatted(identity));
        }
        return this;
    }

    public ScopeOptions plugin(ScopePlugin plugin) {
        Objects.requireNonNull(plugin, "plugin must not be null");
        this.ensureNotDone();
        this.plugins.add(plugin);
        return this;
    }

    void end() {
        this.done = true;
    }

    Map<Identity<?>, Object> bindings() {
        return Collections.unmodifiableMap(this.bindings);
    }

    List<ScopePlugin> plugins() {
        return List.copyOf(this.plugins);
    }

    private void ensureNotDone() {
        if (this.done) {
            throw new IllegalStateException("Input stage has already ended");
        }
    }
}
