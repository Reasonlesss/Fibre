package codes.reason.fibre.scope;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NullMarked
public final class ScopePluginBuilder extends ScopeContext {

    private final List<AutoCloseable> closeables = new ArrayList<>();
    private boolean built;

    ScopePluginBuilder(@Nullable Scope parent, Map<Identity<?>, Object> byIdentity) {
        super(parent, byIdentity, Map.of());
    }

    public ScopePluginBuilder closeable(AutoCloseable closeable) {
        Objects.requireNonNull(closeable, "closeable must not be null");
        this.ensureNotBuilt();
        this.closeables.add(closeable);
        return this;
    }

    public Scope build() {
        this.ensureNotBuilt();
        Scope scope = new Scope(this.parent, this.byIdentity, this.byType, this.closeables);
        this.built = true;
        return scope;
    }

    private void ensureNotBuilt() {
        if (this.built) {
            throw new IllegalStateException("Scope has already been built");
        }
    }

}
