package codes.reason.fibre.identity;

import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Identity<T> {

    Type type();

    Class<? super T> rawType();

    @Nullable
    String name();

    T cast(Object object);
}
