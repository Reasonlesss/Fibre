package codes.reason.fibre.world;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record CuboidRegion(WorldPosition from,
                           WorldPosition to) implements GameRegion {

    public CuboidRegion {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (!from.world().equals(to.world())) {
            throw new IllegalArgumentException("The two points in a region must be in the same world");
        }

        WorldPosition min = new WorldPosition(
                from.world(),
                Math.min(from.x(), to.x()),
                Math.min(from.y(), to.y()),
                Math.min(from.z(), to.z())
        );
        WorldPosition max = new WorldPosition(
                from.world(),
                Math.max(from.x(), to.x()),
                Math.max(from.y(), to.y()),
                Math.max(from.z(), to.z())
        );
        from = min;
        to = max;
    }

    @Override
    public WorldAdapter<?> world() {
        return from.world();
    }

    @Override
    public WorldPosition center() {
        return new WorldPosition(
                world(),
                (from.x() + to.x()) / 2,
                (from.y() + to.y()) / 2,
                (from.z() + to.z()) / 2
        );
    }

    @Override
    public boolean contains(WorldPosition position) {
        return world().equals(position.world())
                && position.x() >= from.x() && position.x() <= to.x()
                && position.y() >= from.y() && position.y() <= to.y()
                && position.z() >= from.z() && position.z() <= to.z();
    }

    @Override
    public boolean intersects(GameRegion other) {
        return switch (other) {
            case CuboidRegion cuboid -> world().equals(cuboid.world())
                    && from.x() <= cuboid.to.x() && to.x() >= cuboid.from.x()
                    && from.y() <= cuboid.to.y() && to.y() >= cuboid.from.y()
                    && from.z() <= cuboid.to.z() && to.z() >= cuboid.from.z();
            case UnionRegion union -> union.intersects(this);
            case WorldRegion world -> world.intersects(this);
        };
    }

    @Override
    public long chunkCount() {
        return (long) (to.chunkX() - from.chunkX() + 1) * (to.chunkZ() - from.chunkZ() + 1);
    }

    @Override
    public void forEachChunk(ChunkVisitor visitor) {
        for (int x = from.chunkX(); x <= to.chunkX(); x++) {
            for (int z = from.chunkZ(); z <= to.chunkZ(); z++) {
                visitor.visit(x, z);
            }
        }
    }

    public Stream<WorldPosition> blocks() {
        int minX = (int) Math.floor(from.x());
        int minY = (int) Math.floor(from.y());
        int minZ = (int) Math.floor(from.z());
        int maxX = (int) Math.floor(to.x());
        int maxY = (int) Math.floor(to.y());
        int maxZ = (int) Math.floor(to.z());

        return IntStream.rangeClosed(minX, maxX).boxed()
                .flatMap(x -> IntStream.rangeClosed(minY, maxY).boxed()
                        .flatMap(y -> IntStream.rangeClosed(minZ, maxZ)
                                .mapToObj(z -> new WorldPosition(world(), x, y, z))));
    }

}
