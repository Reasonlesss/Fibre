package codes.reason.fibre.api.scope;

import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface Filter<T> {

    boolean allows(T value);
}
