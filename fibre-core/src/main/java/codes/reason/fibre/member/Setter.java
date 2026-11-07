package codes.reason.fibre.member;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface Setter {

    void set(Object target, @Nullable Object value) throws Throwable;
}
