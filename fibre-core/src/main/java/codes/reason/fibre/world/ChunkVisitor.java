package codes.reason.fibre.world;

import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface ChunkVisitor {

    void visit(int chunkX, int chunkZ);

}
