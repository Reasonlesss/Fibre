package cloud.emilys.fibre.features.state;

import java.lang.reflect.Method;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record StateTransitionMethod(Method method, Optional<Class<?>> fixedTarget) {}
