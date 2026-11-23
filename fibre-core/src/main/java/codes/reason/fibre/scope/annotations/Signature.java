package codes.reason.fibre.scope.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Signature {

    // each parameter must be the given type or a subtype
    Class<?>[] parameters() default {};

    // the return type must be the given type or a subtype
    Class<?> returns() default void.class;
}
