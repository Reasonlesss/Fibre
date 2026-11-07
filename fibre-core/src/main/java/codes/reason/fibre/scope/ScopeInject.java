package codes.reason.fibre.scope;

import codes.reason.fibre.identity.ClassIdentity;
import codes.reason.fibre.identity.Identity;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ScopeInject {

    private final ScopeBuilder builder;
    private boolean done;

    ScopeInject(ScopeBuilder builder) {
        this.builder = builder;
    }

    public <T> ScopeInject bind(Class<T> type, T value) {
        Objects.requireNonNull(type, "type must not be null");
        return this.bind(new ClassIdentity<>(type), value);
    }

    public <T> ScopeInject bind(Identity<T> identity, T value) {
        this.ensureNotDone();
        this.builder.bind(identity, value);
        return this;
    }

    // sees option bindings, earlier plugins and parents
    public <T> Optional<T> get(Identity<T> identity) {
        this.ensureNotDone();
        return this.builder.get(identity);
    }

    public <T> T require(Identity<T> identity) {
        this.ensureNotDone();
        return this.builder.require(identity);
    }

    void end() {
        this.done = true;
    }

    private void ensureNotDone() {
        if (this.done) {
            throw new IllegalStateException("Inject stage has already ended");
        }
    }
}
