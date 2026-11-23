package codes.reason.fibre.builtin.event;

import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.scope.annotations.QueryableMethod;
import codes.reason.fibre.scope.annotations.Signature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@QueryableMethod(@Signature(parameters = Object.class))
public @interface Listen {

    EventOrder order() default EventOrder.NORMAL;
}
