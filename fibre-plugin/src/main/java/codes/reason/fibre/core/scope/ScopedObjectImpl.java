package codes.reason.fibre.core.scope;

import codes.reason.fibre.api.data.RuntimeData;
import codes.reason.fibre.api.fact.Facts;
import codes.reason.fibre.api.scope.Scope;
import codes.reason.fibre.api.scope.ScopedObject;
import codes.reason.fibre.core.data.RuntimeDataImpl;
import codes.reason.fibre.core.util.ResourceCleanup;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ScopedObjectImpl implements ScopedObject {

    private final RuntimeData data = new RuntimeDataImpl();
    private final Deque<AutoCloseable> closeables = new ArrayDeque<>();
    private final Object object;
    private final Facts facts;
    private @Nullable Scope scope;
    private boolean closed;

    public ScopedObjectImpl(Object object, Facts facts) {
        this.object = Objects.requireNonNull(object, "object");
        this.facts = Objects.requireNonNull(facts, "facts");
    }

    @Override
    public void track(AutoCloseable closeable) {
        Objects.requireNonNull(closeable, "closeable");
        if (this.closed) {
            throw new IllegalStateException("Attempted to call track after this object was closed.");
        }
        this.closeables.addLast(closeable);
    }

    @Override
    public Object getObject() {
        return this.object;
    }

    @Override
    public Scope getScope() {
        if (this.scope == null) {
            throw new IllegalStateException("Attempted to call getScope before a scope was bound to this object.");
        }
        return this.scope;
    }

    public void bindTo(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Facts getFacts() {
        return this.facts;
    }

    @Override
    public RuntimeData getRuntimeData() {
        return this.data;
    }

    @Override
    public void close() throws Exception {
        if (this.closed) {
            return;
        }
        this.closed = true;
        ArrayList<AutoCloseable> closeables = new ArrayList<>(this.closeables);
        this.closeables.clear();
        ResourceCleanup.closeAll(closeables);
    }
}
