package codes.reason.fibre.member;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface Invoker {

    @Nullable Object invoke(Object target, @Nullable Object[] args) throws Throwable;

}