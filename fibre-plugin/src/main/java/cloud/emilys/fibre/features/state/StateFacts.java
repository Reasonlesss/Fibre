package cloud.emilys.fibre.features.state;

import cloud.emilys.fibre.api.fact.FactKey;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StateFacts {

    public static final FactKey<Class<?>> STATE_MACHINE_INITIAL_STATE =
            FactKey.of("fibre", "state_machine_object", Class.class);

    public static final FactKey<List<StateTransitionMethod>> STATE_TRANSITION_METHODS =
            FactKey.of("fibre", "state_transition_methods", List.class);

    private StateFacts() {
        throw new UnsupportedOperationException();
    }
}
