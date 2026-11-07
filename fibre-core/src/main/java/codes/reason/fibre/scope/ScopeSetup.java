package codes.reason.fibre.scope;

import codes.reason.fibre.plugin.Attachment;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ScopeSetup extends ScopeContext {

    private final Scope scope;
    private boolean done;

    ScopeSetup(Scope scope) {
        super(scope.parent, scope.byIdentity, scope.instances, scope.methodsByAnnotation, scope.fieldsByAnnotation);
        this.scope = scope;
    }

    public Scope scope() {
        return this.scope;
    }

    public <T> ScopeSetup attach(Attachment<T> attachment, T value) {
        Objects.requireNonNull(attachment, "attachment must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.ensureNotDone();
        Object existing = this.scope.attachments.putIfAbsent(attachment, attachment.cast(value));
        if (existing != null) {
            throw new IllegalStateException("%s is already attached to this scope".formatted(attachment));
        }
        return this;
    }

    public ScopeSetup closeable(AutoCloseable closeable) {
        Objects.requireNonNull(closeable, "closeable must not be null");
        this.ensureNotDone();
        this.scope.closeables.add(closeable);
        return this;
    }

    void end() {
        this.done = true;
    }

    private void ensureNotDone() {
        if (this.done) {
            throw new IllegalStateException("Plugin stage has already ended");
        }
    }
}
