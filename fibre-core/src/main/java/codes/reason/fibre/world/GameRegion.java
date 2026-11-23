package codes.reason.fibre.world;

import org.jspecify.annotations.NullMarked;

@NullMarked
public sealed interface GameRegion permits WorldRegion, CuboidRegion, UnionRegion {

    WorldAdapter<?> world();

    boolean contains(WorldPosition position);

    boolean intersects(GameRegion other);

    WorldPosition center();

    long chunkCount();

    void forEachChunk(ChunkVisitor visitor);

}
