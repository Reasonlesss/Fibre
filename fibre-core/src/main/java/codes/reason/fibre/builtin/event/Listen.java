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

    // Every event of the type, not just this game's: platform events that belong to another game or to none (such as
    // a plugin's own events, which have no player or position), and game events from every game. The scope's filters
    // are about its own players, so they're skipped.
    boolean global() default false;
}
