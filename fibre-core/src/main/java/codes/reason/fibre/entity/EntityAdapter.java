package codes.reason.fibre.entity;

import codes.reason.fibre.Fibre;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface EntityAdapter<E> {

    static EntityAdapter<?> of(Object entity) {
        return Fibre.get().platform().adapters().entity(entity);
    }

    E get();

    UUID id();

    default <T> Optional<T> as(Class<T> type) {
        E entity = this.get();
        if (type.isInstance(entity)) {
            return Optional.of(type.cast(entity));
        }
        return Optional.empty();
    }

}
