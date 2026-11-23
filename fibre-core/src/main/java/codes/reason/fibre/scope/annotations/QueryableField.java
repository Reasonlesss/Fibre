package codes.reason.fibre.scope.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface QueryableField {

    // checked at compile time, an annotated field must be one of these or a subtype; none means any type
    Class<?>[] value() default {};

    // the field can't be final, so plugins can set it
    boolean mutable() default false;

    // at most one field in a class can have the annotation
    boolean unique() default false;
}
