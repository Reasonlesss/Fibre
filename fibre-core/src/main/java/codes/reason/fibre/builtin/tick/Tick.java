package codes.reason.fibre.builtin.tick;

import codes.reason.fibre.scope.annotations.QueryableMethod;
import codes.reason.fibre.scope.annotations.Signature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@QueryableMethod(@Signature)
public @interface Tick {
}
