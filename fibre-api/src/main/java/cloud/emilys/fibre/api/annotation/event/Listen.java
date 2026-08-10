package cloud.emilys.fibre.api.annotation.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.event.EventPriority;
import org.jspecify.annotations.NullMarked;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@NullMarked
public @interface Listen {

    EventPriority priority() default EventPriority.NORMAL;

    boolean ignoreGlobal() default true;
}
