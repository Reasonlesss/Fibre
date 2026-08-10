package cloud.emilys.fibre.features.lifecycle;

import cloud.emilys.fibre.api.fact.FactKey;
import java.lang.reflect.Method;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LifecycleFacts {

    public static final FactKey<List<Method>> CONFIGURE =
            FactKey.of("fibre", "lifecycle_configure_methods", List.class);
    public static final FactKey<List<Method>> ENTER = FactKey.of("fibre", "lifecycle_enter_methods", List.class);
    public static final FactKey<List<Method>> EXIT = FactKey.of("fibre", "lifecycle_exit_methods", List.class);
    public static final FactKey<List<Method>> TICK = FactKey.of("fibre", "lifecycle_tick_methods", List.class);

    private LifecycleFacts() {
        throw new UnsupportedOperationException();
    }
}
