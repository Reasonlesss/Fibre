package cloud.emilys.fibre.features.dependency;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.binding.BindingPriority;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ImplicitBinding implements Binding {

    private final Constructor<?> constructor;
    private final List<ObjectKey> dependencies;

    public ImplicitBinding(Constructor<?> constructor) {
        this.constructor = Objects.requireNonNull(constructor, "constructor");
        this.dependencies = Stream.of(constructor.getAnnotatedParameterTypes())
                .map(ObjectKey::fromAnnotatedType)
                .toList();
    }

    @Override
    public BindingPriority getPriority() {
        return BindingPriority.LOW;
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create().add(this.dependencies);
    }

    @Override
    public BindingResult make(ScopeResolver resolver) {
        Objects.requireNonNull(resolver, "resolver");
        Object[] arguments = this.dependencies.stream()
                .map(resolver::require)
                .map(ScopedObject::getObject)
                .toArray();
        try {
            return BindingResult.immediate(this.constructor.newInstance(arguments));
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot invoke dependency constructor %s".formatted(this.constructor.toGenericString()), exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(
                    "Dependency constructor %s failed".formatted(this.constructor.toGenericString()), cause);
        }
    }
}
