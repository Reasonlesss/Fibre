package cloud.emilys.fibre.api.annotation.state;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@NullMarked
public @interface StateMachine {

    Class<?> value();
}
