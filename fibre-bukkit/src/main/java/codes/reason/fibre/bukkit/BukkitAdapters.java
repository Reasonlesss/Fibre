package codes.reason.fibre.bukkit;

import codes.reason.fibre.entity.EntityAdapter;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.world.WorldAdapter;
import codes.reason.fibre.world.WorldPosition;
import codes.reason.fibre.platform.PlatformAdapters;
import java.util.UUID;
import java.util.function.ToIntFunction;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class BukkitAdapters implements PlatformAdapters {

    private final ToIntFunction<World> minHeight;

    BukkitAdapters(ToIntFunction<World> minHeight) {
        this.minHeight = minHeight;
    }

    @Override
    public WorldAdapter<?> world(Object world) {
        World bukkit = cast(world, World.class);
        return new BukkitWorld(bukkit, this.minHeight.applyAsInt(bukkit));
    }

    @Override
    public EntityAdapter<?> entity(Object entity) {
        return entity instanceof Player player ? new BukkitPlayer(player) : new BukkitEntity(cast(entity, Entity.class));
    }

    @Override
    public PlayerAdapter<?> player(Object player) {
        return new BukkitPlayer(cast(player, Player.class));
    }

    @Override
    public WorldPosition position(Object location) {
        Location bukkit = cast(location, Location.class);
        return new WorldPosition(this.world(bukkit.getWorld()), bukkit.getX(), bukkit.getY(), bukkit.getZ());
    }

    private static <T> T cast(Object value, Class<T> type) {
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("%s is not a %s".formatted(value, type.getName()));
        }
        return type.cast(value);
    }

    record BukkitWorld(World world, int minHeight) implements WorldAdapter<World> {

        @Override
        public World get() {
            return this.world;
        }

        @Override
        public Object location(double x, double y, double z) {
            return new Location(this.world, x, y, z);
        }

        @Override
        public int maxHeight() {
            return this.world.getMaxHeight();
        }

    }

    record BukkitEntity(Entity entity) implements EntityAdapter<Entity> {

        @Override
        public Entity get() {
            return this.entity;
        }

        @Override
        public UUID id() {
            return this.entity.getUniqueId();
        }

    }

    record BukkitPlayer(Player player) implements PlayerAdapter<Player> {

        @Override
        public Player get() {
            return this.player;
        }

        @Override
        public UUID id() {
            return this.player.getUniqueId();
        }

        @Override
        public String username() {
            return this.player.getName();
        }

    }

}
