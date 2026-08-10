package cloud.emilys.fibre.api.annotation.dependency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@NullMarked
public @interface Named {
    String value();
}
