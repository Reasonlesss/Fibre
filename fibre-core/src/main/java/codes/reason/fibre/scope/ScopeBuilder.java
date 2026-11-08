package codes.reason.fibre.scope;

import codes.reason.fibre.identity.Identity;
import codes.reason.fibre.member.BoundField;
import codes.reason.fibre.member.BoundMethod;
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

    private final List<Runnable> enterMethods = new ArrayList<>();
    private final List<Runnable> exitMethods = new ArrayList<>();
    private final Handle<Scope> handle = Handle.scope();
    private boolean done;

    ScopeBuilder(@Nullable Scope parent) {
        super(parent, new HashMap<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
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
        ScopeHelper.add(this.methodsByAnnotation, method, annotations);
        return this;
    }

    @SafeVarargs
    public final ScopeBuilder field(BoundField field, Class<? extends Annotation>... annotations) {
        Objects.requireNonNull(field, "field must not be null");
        this.ensureNotDone();
        ScopeHelper.add(this.fieldsByAnnotation, field, annotations);
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

    public ScopeBuilder enter(Runnable method) {
        Objects.requireNonNull(method, "method must not be null");
        this.ensureNotDone();
        this.enterMethods.add(method);
        return this;
    }

    public ScopeBuilder exit(Runnable method) {
        Objects.requireNonNull(method, "method must not be null");
        this.ensureNotDone();
        this.exitMethods.add(method);
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
                this.enterMethods,
                this.exitMethods,
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
