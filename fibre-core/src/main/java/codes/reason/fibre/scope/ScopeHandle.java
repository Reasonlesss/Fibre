package codes.reason.fibre.scope;

import codes.reason.fibre.identity.ClassIdentity;
import codes.reason.fibre.identity.Identity;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ScopeHandle {

    public static final Identity<ScopeHandle> IDENTITY = new ClassIdentity<>(ScopeHandle.class);

    private volatile @Nullable Scope scope;

    ScopeHandle() {}

    public Optional<Scope> get() {
        return Optional.ofNullable(this.scope);
    }

    public Scope require() {
        Scope scope = this.scope;
        if (scope == null) {
            throw new IllegalStateException("Scope hasn't been built yet");
        }
        return scope;
    }

    public boolean isBuilt() {
        return this.scope != null;
    }

    void set(Scope scope) {
        this.scope = scope;
    }
}
