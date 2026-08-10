package cloud.emilys.fibre.features.dependency;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ExplicitBinding implements Binding {

    private final Method method;
    private final @Nullable ObjectKey instance;
    private final List<ObjectKey> parameters;
    private final DependencySet dependencies;

    public ExplicitBinding(Method method) {
        this.method = Objects.requireNonNull(method, "method");
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("An instance provider method requires an instance dependency");
        }
        this.instance = null;
        this.parameters = getParameters(method);
        this.dependencies = DependencySet.create().add(this.parameters);
    }

    public ExplicitBinding(ObjectKey instance, Method method) {
        this.method = Objects.requireNonNull(method, "method");
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("A static provider method must not have an instance dependency");
        }
        this.instance = Objects.requireNonNull(instance, "instance");
        this.parameters = getParameters(method);
        this.dependencies = DependencySet.create().add(this.instance).add(this.parameters);
    }

    private static List<ObjectKey> getParameters(Method method) {
        return Stream.of(method.getAnnotatedParameterTypes())
                .map(ObjectKey::fromAnnotatedType)
                .toList();
    }

    static ObjectKey getProvidedKey(Method method) {
        Objects.requireNonNull(method, "method");
        AnnotatedType returnType = method.getAnnotatedReturnType();
        if (!CompletionStage.class.isAssignableFrom(method.getReturnType())) {
            return ObjectKey.fromAnnotatedType(returnType);
        }
        if (!(returnType instanceof AnnotatedParameterizedType parameterizedType)
                || parameterizedType.getAnnotatedActualTypeArguments().length != 1) {
            throw new IllegalArgumentException("Asynchronous provider methods must declare one result type: %s"
                    .formatted(method.toGenericString()));
        }
        return ObjectKey.fromAnnotatedType(parameterizedType.getAnnotatedActualTypeArguments()[0]);
    }

    @Override
    public DependencySet getDependencies() {
        return this.dependencies;
    }

    @Override
    public BindingResult make(ScopeResolver resolver) {
        Objects.requireNonNull(resolver, "resolver");
        Object receiver =
                this.instance == null ? null : resolver.require(this.instance).getObject();
        Object[] arguments = this.parameters.stream()
                .map(resolver::require)
                .map(ScopedObject::getObject)
                .toArray();
        try {
            Object result = this.method.invoke(receiver, arguments);
            if (result instanceof CompletionStage<?> stage) {
                return BindingResult.deferred(() -> stage.thenApply(value -> (Object) value));
            }
            return BindingResult.immediate(result);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot invoke dependency provider %s".formatted(this.method.toGenericString()), exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(
                    "Dependency provider %s failed".formatted(this.method.toGenericString()), cause);
        }
    }
}
