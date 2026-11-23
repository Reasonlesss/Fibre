package codes.reason.fibre.scope;

import codes.reason.fibre.scope.identity.Identity;
import codes.reason.fibre.scope.member.BoundField;
import codes.reason.fibre.scope.member.BoundMethod;
import codes.reason.fibre.plugin.Attachment;
import codes.reason.fibre.plugin.ScopePlugin;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Scope extends ScopeContext {

    final List<AutoCloseable> closeables = new ArrayList<>();
    final Map<Attachment<?>, Object> attachments = new HashMap<>();
    private final List<Scope> children = new ArrayList<>();
    final List<ScopeLifecycle> lifecycles;
    final List<ScopePlugin> plugins;
    final Handle<Scope> handle;
    int entered;
    private boolean closed;

    Scope(
            @Nullable Scope parent,
            Map<Identity<?>, Object> byIdentity,
            List<Object> instances,
            Map<Class<? extends Annotation>, List<BoundMethod>> methodsByAnnotation,
            Map<Class<? extends Annotation>, List<BoundField>> fieldsByAnnotation,
            List<ScopeLifecycle> lifecycles,
            List<ScopePlugin> plugins,
            Handle<Scope> handle) {
        super(
                parent,
                Map.copyOf(byIdentity),
                List.copyOf(instances),
                ScopeHelper.copyLists(methodsByAnnotation),
                ScopeHelper.copyLists(fieldsByAnnotation));
        this.lifecycles = List.copyOf(lifecycles);
        this.plugins = List.copyOf(plugins);
        this.handle = handle;
        if (parent != null) {
            parent.ensureOpen();
            parent.children.add(this);
        }
    }

    public static Scope root(Class<?> type) {
        return root(type, _ -> {});
    }

    public static Scope root(Class<?> type, Consumer<? super ScopeOptions> options) {
        return ScopeFactory.create(null, type, options);
    }

    public Scope child(Class<?> type) {
        return this.child(type, _ -> {});
    }

    public Scope child(Class<?> type, Consumer<? super ScopeOptions> options) {
        this.ensureOpen();
        return ScopeFactory.create(this, type, options);
    }

    public <T> Optional<T> attachment(Attachment<T> attachment) {
        Objects.requireNonNull(attachment, "attachment must not be null");
        Object value = this.attachments.get(attachment);
        return value != null ? Optional.of(attachment.cast(value)) : Optional.empty();
    }

    public Optional<Scope> parent() {
        return Optional.ofNullable(this.parent);
    }

    public List<Scope> children() {
        return List.copyOf(this.children);
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;

        List<Throwable> failures = new ArrayList<>();
        for (Scope child : List.copyOf(this.children).reversed()) {
            try {
                child.close();
            } catch (Exception e) {
                failures.add(e);
            }
        }
        // only lifecycles that started entering are exited, in reverse, so a scope whose plugins failed exits nothing
        for (ScopeLifecycle lifecycle : this.lifecycles.subList(0, this.entered).reversed()) {
            for (Runnable method : lifecycle.exit().reversed()) {
                try {
                    method.run();
                } catch (Throwable t) {
                    failures.add(t);
                }
            }
        }
        this.entered = 0;
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

    private void ensureOpen() {
        if (this.closed) {
            throw new IllegalStateException("Scope is closed");
        }
    }
}
