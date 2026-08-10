package cloud.emilys.fibre.features.state;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class StateManager implements AutoCloseable {

    private final Scope parent;
    private Scope currentState;
    private ObjectKey currentStateKey;

    public StateManager(Scope parent, Class<?> state) {
        this.parent = parent;
        this.set(state);
    }

    public void set(Class<?> state) {
        if (this.currentState != null) {
            this.currentState.close();
        }
        ObjectKey stateKey = ObjectKey.fromType(state);
        this.currentState = ScopeFactory.synchronous()
                .initialize(ScopeBlueprint.builder()
                        .setParent(this.parent)
                        .setGame(this.parent.getGame())
                        .setInitialKey(stateKey)
                        .build());
        this.currentState.setName(state.getSimpleName());
        this.currentStateKey = stateKey;
    }

    public void tick() {
        ScopedObject object = this.currentState.require(this.currentStateKey);
        List<StateTransitionMethod> transitions =
                object.getFacts().get(StateFacts.STATE_TRANSITION_METHODS).orElse(List.of());
        for (StateTransitionMethod transition : transitions) {
            Optional<Class<?>> target = this.getTransitionTarget(object, transition);
            if (target.isPresent()) {
                this.set(target.get());
                return;
            }
        }
    }

    private Optional<Class<?>> getTransitionTarget(ScopedObject object, StateTransitionMethod transition) {
        Object result = this.invoke(object, transition);
        if (transition.fixedTarget().isPresent()) {
            assert result != null;
            return (boolean) result ? transition.fixedTarget() : Optional.empty();
        }
        if (transition.method().getReturnType() == Class.class) {
            return Optional.ofNullable((Class<?>) result);
        }
        if (result == null) {
            throw new IllegalStateException("State transition method %s returned null instead of Optional.empty()"
                    .formatted(transition.method().toGenericString()));
        }
        Optional<?> target = (Optional<?>) result;
        return target.map(value -> {
            if (value instanceof Class<?> state) {
                return state;
            }
            throw new IllegalStateException(
                    "State transition method %s returned an Optional containing %s instead of Class<?>"
                            .formatted(
                                    transition.method().toGenericString(),
                                    value.getClass().getTypeName()));
        });
    }

    private @Nullable Object invoke(ScopedObject object, StateTransitionMethod transition) {
        try {
            return transition.method().invoke(object.getObject());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot invoke state transition method %s"
                            .formatted(transition.method().toGenericString()),
                    exception);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (exception.getCause() instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(
                    "Cannot invoke state transition method %s"
                            .formatted(transition.method().toGenericString()),
                    exception);
        }
    }

    @Override
    public void close() {
        this.currentState.close();
    }
}
