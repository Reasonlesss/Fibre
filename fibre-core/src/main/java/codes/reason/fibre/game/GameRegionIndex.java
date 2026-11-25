package codes.reason.fibre.game;

import codes.reason.fibre.world.GameRegion;
import codes.reason.fibre.world.WorldAdapter;
import codes.reason.fibre.world.WorldPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class GameRegionIndex {

    private static final int MAX_BUCKETED_CHUNKS = 64 * 64;

    private final Map<WorldAdapter<?>, Bucket> worlds = new HashMap<>();

    void add(GameHolder game) {
        for (GameRegion region : game.regions()) {
            Bucket bucket = this.worlds.computeIfAbsent(region.world(), _ -> new Bucket());
            Entry entry = new Entry(game, region);
            if (region.chunkCount() > MAX_BUCKETED_CHUNKS) {
                bucket.unbucketed.add(entry);
                continue;
            }
            Set<Long> chunks = new HashSet<>();
            region.forEachChunk((x, z) -> chunks.add(chunkKey(x, z)));
            for (long chunk : chunks) {
                bucket.chunks.computeIfAbsent(chunk, _ -> new ArrayList<>()).add(entry);
            }
        }
    }

    void remove(GameHolder game) {
        this.worlds.values().removeIf(bucket -> {
            bucket.unbucketed.removeIf(entry -> entry.game() == game);
            bucket.chunks.values().removeIf(entries -> {
                entries.removeIf(entry -> entry.game() == game);
                return entries.isEmpty();
            });
            return bucket.isEmpty();
        });
    }

    Optional<GameHolder> find(WorldPosition position) {
        Bucket bucket = this.worlds.get(position.world());
        if (bucket == null) {
            return Optional.empty();
        }
        List<Entry> entries = bucket.chunks.get(chunkKey(position.chunkX(), position.chunkZ()));
        if (entries != null) {
            for (Entry entry : entries) {
                if (entry.region().contains(position)) {
                    return Optional.of(entry.game());
                }
            }
        }
        for (Entry entry : bucket.unbucketed) {
            if (entry.region().contains(position)) {
                return Optional.of(entry.game());
            }
        }
        return Optional.empty();
    }

    void ensureNoOverlap(GameIdentity identity, GameRegion region) {
        Bucket bucket = this.worlds.get(region.world());
        if (bucket == null) {
            return;
        }
        List<Entry> candidates = new ArrayList<>(bucket.unbucketed);
        if (region.chunkCount() > MAX_BUCKETED_CHUNKS) {
            bucket.chunks.values().forEach(candidates::addAll);
        } else {
            region.forEachChunk((x, z) -> {
                List<Entry> entries = bucket.chunks.get(chunkKey(x, z));
                if (entries != null) {
                    candidates.addAll(entries);
                }
            });
        }
        for (Entry candidate : candidates) {
            if (candidate.region().intersects(region)) {
                throw new IllegalStateException("A region of %s overlaps a region of %s"
                        .formatted(identity, candidate.game().identity()));
            }
        }
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private record Entry(GameHolder game, GameRegion region) {}

    private static final class Bucket {

        private final Map<Long, List<Entry>> chunks = new HashMap<>();
        private final List<Entry> unbucketed = new ArrayList<>();

        private boolean isEmpty() {
            return this.chunks.isEmpty() && this.unbucketed.isEmpty();
        }

    }

}
