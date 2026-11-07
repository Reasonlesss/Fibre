package codes.reason.fibre.plugin;

import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Attachment<T> {

    Class<?> owner();

    Type type();

    Class<? super T> rawType();

    @Nullable
    String name();

    T cast(Object object);
}
