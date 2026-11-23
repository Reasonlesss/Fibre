package codes.reason.fibre.world;

import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record UnionRegion(List<GameRegion> regions) implements GameRegion {

    public UnionRegion {
        regions = List.copyOf(regions);
        if (regions.isEmpty()) {
            throw new IllegalArgumentException("A union region must contain at least one region");
        }

        WorldAdapter<?> world = regions.getFirst().world();
        for (GameRegion region : regions) {
            if (!world.equals(region.world())) {
                throw new IllegalArgumentException("All regions in a union must be in the same world");
            }
        }
    }

    public UnionRegion(GameRegion... regions) {
        this(List.of(regions));
    }

    @Override
    public WorldAdapter<?> world() {
        return regions.getFirst().world();
    }

    @Override
    public boolean contains(WorldPosition position) {
        for (GameRegion region : regions) {
            if (region.contains(position)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public WorldPosition center() {
        double x = 0;
        double y = 0;
        double z = 0;
        for (GameRegion region : regions) {
            WorldPosition center = region.center();
            x += center.x();
            y += center.y();
            z += center.z();
        }
        int count = regions.size();
        return new WorldPosition(world(), x / count, y / count, z / count);
    }

    @Override
    public boolean intersects(GameRegion other) {
        for (GameRegion region : regions) {
            if (region.intersects(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long chunkCount() {
        long count = 0;
        for (GameRegion region : regions) {
            long members = region.chunkCount();
            if (members == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            // an upper bound, as members sharing a chunk count it more than once
            count += members;
        }
        return count;
    }

    @Override
    public void forEachChunk(ChunkVisitor visitor) {
        for (GameRegion region : regions) {
            region.forEachChunk(visitor);
        }
    }

}
