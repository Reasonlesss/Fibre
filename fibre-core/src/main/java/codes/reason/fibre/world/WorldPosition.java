package codes.reason.fibre.world;

import codes.reason.fibre.Fibre;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record WorldPosition(WorldAdapter<?> world,
                            double x,
                            double y,
                            double z) {

    public WorldPosition {
        Objects.requireNonNull(world, "world must not be null");
    }

    public static WorldPosition of(Object location) {
        return Fibre.get().platform().adapters().position(location);
    }

    public int chunkX() {
        return (int) Math.floor(this.x) >> 4;
    }

    public int chunkZ() {
        return (int) Math.floor(this.z) >> 4;
    }

    public WorldPosition offset(double x, double y, double z) {
        return new WorldPosition(world, this.x + x, this.y + y, this.z + z);
    }

    public <T> Optional<T> as(Class<T> type) {
        Object location = world.location(x, y, z);
        if (type.isInstance(location)) {
            return Optional.of(type.cast(location));
        }
        return Optional.empty();
    }

}
