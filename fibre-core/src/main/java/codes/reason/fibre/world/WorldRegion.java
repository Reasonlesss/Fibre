package codes.reason.fibre.world;

import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record WorldRegion(WorldAdapter<?> world) implements GameRegion {

    public WorldRegion {
        Objects.requireNonNull(world, "world");
    }

    @Override
    public boolean contains(WorldPosition position) {
        return world.equals(position.world());
    }

    @Override
    public WorldPosition center() {
        return new WorldPosition(world, 0, (world.minHeight() + world.maxHeight()) / 2.0, 0);
    }

    @Override
    public boolean intersects(GameRegion other) {
        return world.equals(other.world());
    }

    @Override
    public long chunkCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public void forEachChunk(ChunkVisitor visitor) {
        throw new UnsupportedOperationException("A world region covers every chunk");
    }

}
