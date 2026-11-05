package codes.reason.fibre.member;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface Getter {

    @Nullable Object get(Object target) throws Throwable;

}