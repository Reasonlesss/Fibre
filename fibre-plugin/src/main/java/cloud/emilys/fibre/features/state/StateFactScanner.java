package cloud.emilys.fibre.features.state;

import cloud.emilys.fibre.api.annotation.state.FromReturnValue;
import cloud.emilys.fibre.api.annotation.state.StateMachine;
import cloud.emilys.fibre.api.annotation.state.Transition;
import cloud.emilys.fibre.api.fact.FactScanContext;
import cloud.emilys.fibre.api.fact.FactScanner;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StateFactScanner implements FactScanner {

    @Override
    public void collect(FactScanContext context) {
        StateMachine stateMachine = context.getType().getAnnotation(StateMachine.class);
        if (stateMachine != null) {
            context.put(StateFacts.STATE_MACHINE_INITIAL_STATE, stateMachine.value());
        }

        List<StateTransitionMethod> transitions = new ArrayList<>();
        for (Method method : context.getMethods()) {
            Transition transition = method.getAnnotation(Transition.class);
            if (transition == null) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                context.report(method, "State transition methods must not be static.");
            }
            if (method.getParameterCount() != 0) {
                context.report(method, "State transition methods must not contain any parameters.");
            }
            boolean targetFromReturnValue = transition.to() == FromReturnValue.class;
            if (targetFromReturnValue && !returnsTransitionTarget(method)) {
                context.report(
                        method,
                        "State transition methods without a fixed target must return Class<?> or Optional<Class<?>>.");
            } else if (!targetFromReturnValue && method.getReturnType() != boolean.class) {
                context.report(method, "State transition methods with a fixed target must return boolean.");
            }
            method.setAccessible(true);
            transitions.add(new StateTransitionMethod(
                    method, targetFromReturnValue ? Optional.empty() : Optional.of(transition.to())));
        }
        if (!transitions.isEmpty()) {
            context.put(StateFacts.STATE_TRANSITION_METHODS, List.copyOf(transitions));
        }
    }

    private static boolean returnsTransitionTarget(Method method) {
        if (method.getReturnType() == Class.class) {
            return true;
        }
        if (method.getReturnType() != Optional.class) {
            return false;
        }
        Type returnType = method.getGenericReturnType();
        if (!(returnType instanceof ParameterizedType optionalType)) {
            return false;
        }
        Type[] arguments = optionalType.getActualTypeArguments();
        if (arguments.length != 1 || !(arguments[0] instanceof ParameterizedType classType)) {
            return false;
        }
        return classType.getRawType() == Class.class;
    }
}
