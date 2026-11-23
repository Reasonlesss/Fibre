package codes.reason.fibre.builtin.state;

import codes.reason.fibre.scope.Handle;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.annotations.Enter;
import codes.reason.fibre.scope.annotations.Exit;
import codes.reason.fibre.scope.annotations.Managed;
import codes.reason.fibre.scope.annotations.Use;
import codes.reason.fibre.scope.identity.Identity;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Managed(many = true)
public final class State<T> {

    private final Handle<Scope> owner;
    private final Class<?> type;
    private final StateTransitionPlugin transitions = new StateTransitionPlugin(this);
    private @Nullable Scope scope;
    private @Nullable T value;
    // bumped by every switch, so a switch made from inside another one wins
    private int generation;

    @Use
    State(Identity<State<T>> identity, Handle<Scope> owner) {
        this.owner = owner;
        this.type = stateType(identity);
    }

    public T get() {
        T value = this.value;
        if (value == null) {
            throw new IllegalStateException("%s has no current state".formatted(this));
        }
        return value;
    }

    public void set(Class<? extends T> type) {
        if (!this.type.isAssignableFrom(type)) {
            throw new IllegalArgumentException("%s is not a %s".formatted(type.getName(), this.type.getSimpleName()));
        }
        int generation = ++this.generation;
        this.closeCurrent();
        if (generation != this.generation) {
            return;
        }
        Scope scope = this.owner.require().child(type, options -> options.plugin(this.transitions));
        if (generation != this.generation || scope.isClosed()) {
            scope.close();
            return;
        }
        this.scope = scope;
        this.value = scope.require(type);
    }

    Scope owner() {
        return this.owner.require();
    }

    @SuppressWarnings("unchecked")
    void transition(Class<?> type) {
        this.set((Class<? extends T>) type);
    }

    @Enter
    void enter() {
        Initially initially = this.type.getAnnotation(Initially.class);
        if (initially == null) {
            throw new IllegalStateException("%s needs @Initially".formatted(this.type.getName()));
        }
        this.transition(initially.value());
    }

    @Exit
    void exit() {
        this.generation++;
        this.closeCurrent();
    }

    private void closeCurrent() {
        Scope previous = this.scope;
        this.scope = null;
        this.value = null;
        if (previous != null) {
            previous.close();
        }
    }

    @Override
    public String toString() {
        return "State<%s>".formatted(this.type.getSimpleName());
    }

    private static Class<?> stateType(Identity<?> identity) {
        Type argument = ((ParameterizedType) identity.type()).getActualTypeArguments()[0];
        return argument instanceof ParameterizedType parameterized
                ? (Class<?>) parameterized.getRawType()
                : (Class<?>) argument;
    }

}
