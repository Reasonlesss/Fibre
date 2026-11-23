package codes.reason.fibre.builtin.state;

import codes.reason.fibre.scope.annotations.QueryableMethod;
import codes.reason.fibre.scope.annotations.Signature;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@QueryableMethod({
        @Signature(returns = boolean.class),
        @Signature(returns = Class.class),
        @Signature(returns = Optional.class)
})
public @interface Transition {

    Class<?> to() default void.class;
}
