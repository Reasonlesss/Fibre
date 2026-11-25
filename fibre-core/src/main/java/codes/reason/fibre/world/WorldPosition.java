package codes.reason.fibre.world;

import codes.reason.fibre.Fibre;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
// yaw and pitch are which way something there faces, such as a spawn; most positions leave them at 0
public record WorldPosition(WorldAdapter<?> world,
                            double x,
                            double y,
                            double z,
                            float yaw,
                            float pitch) {

    public WorldPosition {
        Objects.requireNonNull(world, "world must not be null");
    }

    public WorldPosition(WorldAdapter<?> world, double x, double y, double z) {
        this(world, x, y, z, 0, 0);
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
        return new WorldPosition(world, this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }

    public <T> Optional<T> as(Class<T> type) {
        Object location = world.location(x, y, z, yaw, pitch);
        if (type.isInstance(location)) {
            return Optional.of(type.cast(location));
        }
        return Optional.empty();
    }

}
