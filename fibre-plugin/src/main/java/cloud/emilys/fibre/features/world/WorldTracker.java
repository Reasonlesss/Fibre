package cloud.emilys.fibre.features.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldTracker {

    private final Map<UUID, Integer> attachments = new HashMap<>();

    public void attach(World world) {
        this.attachments.merge(worldId(world), 1, Integer::sum);
    }

    public void detach(UUID worldId) {
        this.attachments.computeIfPresent(
                Objects.requireNonNull(worldId, "worldId"), (_, count) -> count == 1 ? null : count - 1);
    }

    public boolean contains(World world) {
        return this.attachments.containsKey(worldId(world));
    }

    private static UUID worldId(World world) {
        return Objects.requireNonNull(world, "world").getUID();
    }
}
