package codes.reason.fibre.scope;

import codes.reason.fibre.scope.annotations.QueryableField;
import codes.reason.fibre.scope.annotations.QueryableMethod;
import codes.reason.fibre.scope.identity.Identity;
import codes.reason.fibre.scope.member.BoundField;
import codes.reason.fibre.scope.member.BoundMethod;
import codes.reason.fibre.plugin.ScopePlugin;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ScopeBuilder extends ScopeContext {

    private final List<ScopeLifecycle> lifecycles = new ArrayList<>();
    private final Handle<Scope> handle;
    private boolean done;

    ScopeBuilder(@Nullable Scope parent, Handle<Scope> handle) {
        super(parent, new HashMap<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
        this.handle = handle;
        // not an instance, so list() never finds it
        this.byIdentity.put(Handle.SCOPE, this.handle);
    }

    public <T> ScopeBuilder bind(Identity<T> identity, T value) {
        Objects.requireNonNull(identity, "identity must not be null");
        Objects.requireNonNull(value, "value must not be null");
        this.ensureNotDone();
        Object existing = this.byIdentity.putIfAbsent(identity, identity.cast(value));
        if (existing != null) {
            throw new IllegalStateException("%s is already bound in this scope".formatted(identity));
        }
        this.addInstance(value);
        return this;
    }

    public ScopeBuilder instance(Object value) {
        Objects.requireNonNull(value, "value must not be null");
        this.ensureNotDone();
        this.addInstance(value);
        return this;
    }

    @SafeVarargs
    public final ScopeBuilder method(BoundMethod method, Class<? extends Annotation>... annotations) {
        Objects.requireNonNull(method, "method must not be null");
        this.ensureNotDone();
        ScopeHelper.add(this.methodsByAnnotation, method, QueryableMethod.class, annotations);
        return this;
    }

    @SafeVarargs
    public final ScopeBuilder field(BoundField field, Class<? extends Annotation>... annotations) {
        Objects.requireNonNull(field, "field must not be null");
        this.ensureNotDone();
        ScopeHelper.add(this.fieldsByAnnotation, field, QueryableField.class, annotations);
        return this;
    }

    // resolves once the scope is built, so values bound later in the scope can be referred to
    public Handle<Scope> handle() {
        return this.handle;
    }

    public <T> Handle<T> handle(Identity<T> identity) {
        Objects.requireNonNull(identity, "identity must not be null");
        return this.handle.map(scope -> scope.require(identity));
    }

    public ScopeBuilder lifecycle(List<Runnable> enter, List<Runnable> exit) {
        Objects.requireNonNull(enter, "enter must not be null");
        Objects.requireNonNull(exit, "exit must not be null");
        this.ensureNotDone();
        this.lifecycles.add(new ScopeLifecycle(enter, exit));
        return this;
    }

    Scope build(List<ScopePlugin> plugins) {
        this.ensureNotDone();
        this.done = true;
        return new Scope(
                this.parent,
                this.byIdentity,
                this.instances,
                this.methodsByAnnotation,
                this.fieldsByAnnotation,
                this.lifecycles,
                plugins,
                this.handle);
    }

    private void addInstance(Object value) {
        for (Object instance : this.instances) {
            if (instance == value) {
                return;
            }
        }
        this.instances.add(value);
    }

    private void ensureNotDone() {
        if (this.done) {
            throw new IllegalStateException("Blueprint stage has already ended");
        }
    }
}
