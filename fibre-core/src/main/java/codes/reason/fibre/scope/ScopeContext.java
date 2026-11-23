package codes.reason.fibre.scope;

import codes.reason.fibre.scope.annotations.QueryableField;
import codes.reason.fibre.scope.annotations.QueryableMethod;
import codes.reason.fibre.scope.identity.ClassIdentity;
import codes.reason.fibre.scope.identity.Identity;
import codes.reason.fibre.scope.member.BoundField;
import codes.reason.fibre.scope.member.BoundMethod;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
abstract sealed class ScopeContext permits Scope, ScopeBuilder, ScopeSetup {

    protected final @Nullable Scope parent;
    protected final Map<Identity<?>, Object> byIdentity;
    protected final List<Object> instances;
    protected final Map<Class<? extends Annotation>, List<BoundMethod>> methodsByAnnotation;
    protected final Map<Class<? extends Annotation>, List<BoundField>> fieldsByAnnotation;

    ScopeContext(
            @Nullable Scope parent,
            Map<Identity<?>, Object> byIdentity,
            List<Object> instances,
            Map<Class<? extends Annotation>, List<BoundMethod>> methodsByAnnotation,
            Map<Class<? extends Annotation>, List<BoundField>> fieldsByAnnotation) {
        this.parent = parent;
        this.byIdentity = byIdentity;
        this.instances = instances;
        this.methodsByAnnotation = methodsByAnnotation;
        this.fieldsByAnnotation = fieldsByAnnotation;
    }

    public final <T> Optional<T> get(Identity<T> identity) {
        Objects.requireNonNull(identity, "identity must not be null");
        Object value = this.byIdentity.get(identity);
        if (value != null) {
            return Optional.of(identity.cast(value));
        }
        return this.parent != null ? this.parent.get(identity) : Optional.empty();
    }

    public final <T> Optional<T> get(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        return this.get(new ClassIdentity<>(type));
    }

    public final <T> Optional<T> get(String name, Class<T> type) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        return this.get(new ClassIdentity<>(name, type));
    }

    public final <T> T require(Identity<T> identity) {
        return this.get(identity)
                .orElseThrow(() -> new IllegalStateException(
                        "No value found for %s in this scope or in parents".formatted(identity)));
    }

    public final <T> T require(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        return this.require(new ClassIdentity<>(type));
    }

    public final <T> T require(String name, Class<T> type) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        return this.require(new ClassIdentity<>(name, type));
    }

    public final <T> List<T> list(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        List<T> result = new ArrayList<>();
        for (ScopeContext context = this; context != null; context = context.parent) {
            context.collect(type, result);
        }
        return Collections.unmodifiableList(result);
    }

    public final <T> List<T> localList(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        List<T> result = new ArrayList<>();
        this.collect(type, result);
        return Collections.unmodifiableList(result);
    }

    private <T> void collect(Class<T> type, List<T> result) {
        for (Object value : this.instances) {
            if (type.isInstance(value)) {
                result.add(type.cast(value));
            }
        }
    }

    public final List<BoundMethod> methods(Class<? extends Annotation> annotation) {
        ScopeHelper.queryable(annotation, QueryableMethod.class);
        List<BoundMethod> result = new ArrayList<>();
        for (ScopeContext context = this; context != null; context = context.parent) {
            result.addAll(context.methodsByAnnotation.getOrDefault(annotation, List.of()));
        }
        return Collections.unmodifiableList(result);
    }

    public final List<BoundMethod> localMethods(Class<? extends Annotation> annotation) {
        ScopeHelper.queryable(annotation, QueryableMethod.class);
        return List.copyOf(this.methodsByAnnotation.getOrDefault(annotation, List.of()));
    }

    public final List<BoundField> fields(Class<? extends Annotation> annotation) {
        ScopeHelper.queryable(annotation, QueryableField.class);
        List<BoundField> result = new ArrayList<>();
        for (ScopeContext context = this; context != null; context = context.parent) {
            result.addAll(context.fieldsByAnnotation.getOrDefault(annotation, List.of()));
        }
        return Collections.unmodifiableList(result);
    }

    public final List<BoundField> localFields(Class<? extends Annotation> annotation) {
        ScopeHelper.queryable(annotation, QueryableField.class);
        return List.copyOf(this.fieldsByAnnotation.getOrDefault(annotation, List.of()));
    }
}
