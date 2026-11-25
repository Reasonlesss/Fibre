package codes.reason.fibre.world;

import codes.reason.fibre.Fibre;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorldAdapter<W> {

    static WorldAdapter<?> of(Object world) {
        return Fibre.get().platform().adapters().world(world);
    }

    W get();

    Object location(double x, double y, double z);

    default Object location(double x, double y, double z, float yaw, float pitch) {
        return this.location(x, y, z);
    }

    int minHeight();

    int maxHeight();

    // the world's own spawn point
    WorldPosition spawn();

    default <T> Optional<T> as(Class<T> type) {
        W world = this.get();
        if (type.isInstance(world)) {
            return Optional.of(type.cast(world));
        }
        return Optional.empty();
    }

}
