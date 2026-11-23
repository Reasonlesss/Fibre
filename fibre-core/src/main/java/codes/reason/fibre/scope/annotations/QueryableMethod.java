package codes.reason.fibre.scope.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface QueryableMethod {

    // checked at compile time, an annotated method must match one of these; none means any method
    Signature[] value() default {};

    // at most one method in a class can have the annotation
    boolean unique() default false;
}
