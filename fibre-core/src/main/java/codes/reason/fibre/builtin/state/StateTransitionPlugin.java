package codes.reason.fibre.builtin.state;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.game.GameExecutor;
import codes.reason.fibre.plugin.ScopePlugin;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.ScopeSetup;
import codes.reason.fibre.scope.member.BoundMethod;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class StateTransitionPlugin implements ScopePlugin {

    private final State<?> state;

    StateTransitionPlugin(State<?> state) {
        this.state = state;
    }

    @Override
    public void setup(Fibre fibre, ScopeSetup setup) {
        Scope scope = setup.scope();
        // plugins are inherited, so only the state's own scope switches it
        if (scope.parent().orElse(null) != this.state.owner()) {
            return;
        }
        List<Target> targets = setup.localMethods(Transition.class).stream().map(StateTransitionPlugin::target).toList();
        if (targets.isEmpty()) {
            return;
        }

        GameExecutor executor = setup.require(GameExecutor.class);
        setup.closeable(fibre.platform().scheduler().runRepeating(0L, 1L, () -> executor.execute(() -> {
            for (Target target : targets) {
                Class<?> next = target.get();
                if (scope.isClosed()) {
                    return;
                }
                if (next != null) {
                    this.state.transition(next);
                    return;
                }
            }
        })));
    }

    private static Target target(BoundMethod method) {
        Class<?> to = method.method().getAnnotation(Transition.class).to();
        Class<?> returns = method.method().getReturnType();
        // the processor allows each return type, but which one depends on whether there's a target
        if (to != void.class) {
            method.signature()
                    .returns(boolean.class)
                    .check("@Transition with a target");
            //noinspection DataFlowIssue
            return () -> (boolean) method.invoke() ? to : null;
        }
        if (returns == Optional.class) {
            return () -> ((Optional<?>) Objects.requireNonNull(method.invoke()))
                    .map(Class.class::cast)
                    .orElse(null);
        }
        method.signature()
                .returns(Class.class)
                .check("@Transition without a target");
        return () -> (Class<?>) method.invoke();
    }

    @FunctionalInterface
    private interface Target {

        @Nullable Class<?> get() throws Throwable;

    }

}
