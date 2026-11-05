package codes.reason.fibre.scope;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public final class Scope extends ScopeContext implements AutoCloseable {

    private final List<Scope> children = new ArrayList<>();
    private final List<AutoCloseable> closeables;
    private boolean closed;

    Scope(
            @Nullable Scope parent,
            Map<Identity<?>, Object> byIdentity,
            Map<Class<?>, List<Object>> byType,
            List<AutoCloseable> closeables
    ) {
        super(parent, Map.copyOf(byIdentity), copyLists(byType));
        this.closeables = new ArrayList<>(closeables);
        if (parent != null) {
            parent.ensureOpen();
            parent.children.add(this);
        }
    }

    public static ScopeBuilder root() {
        return new ScopeBuilder(null);
    }

    public ScopeBuilder child() {
        this.ensureOpen();
        return new ScopeBuilder(this);
    }

    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;

        List<Exception> failures = new ArrayList<>();
        for (Scope child : List.copyOf(this.children).reversed()) {
            try {
                child.close();
            } catch (Exception e) {
                failures.add(e);
            }
        }
        for (AutoCloseable closeable : this.closeables.reversed()) {
            try {
                closeable.close();
            } catch (Exception e) {
                failures.add(e);
            }
        }

        this.children.clear();
        this.closeables.clear();
        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        if (!failures.isEmpty()) {
            IllegalStateException exception = new IllegalStateException("Failed to close scope cleanly");
            failures.forEach(exception::addSuppressed);
            throw exception;
        }
    }

    private static Map<Class<?>, List<Object>> copyLists(Map<Class<?>, List<Object>> byType) {
        Map<Class<?>, List<Object>> copy = new HashMap<>();
        byType.forEach((type, values) -> copy.put(type, List.copyOf(values)));
        return Map.copyOf(copy);
    }

    private void ensureOpen() {
        if (this.closed) {
            throw new IllegalStateException("Scope is closed");
        }
    }

}
