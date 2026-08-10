package cloud.emilys.fibre.features.world;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.data.RuntimeDataKey;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.world.WorldOwner;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldFeature {

    private static final RuntimeDataKey<WorldTracker> WORLDS = RuntimeDataKey.of("fibre", "worlds", WorldTracker.class);

    private WorldFeature() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        Objects.requireNonNull(fibre, "fibre");
        fibre.getStartupRegistry().registerScopeContributor((collector, key) -> {
            collector.postInitialize(key, (_, object) -> {
                World world =
                        switch (object.getObject()) {
                            case World w -> w;
                            case WorldOwner owner -> owner.getWorld();
                            default -> null;
                        };
                if (world == null) {
                    return;
                }
                Game game = object.getGame();
                WorldTracker worldTracker = game.getOrPut(WORLDS, WorldTracker::new);
                worldTracker.attach(world);
                UUID worldId = world.getUID();
                object.track(() -> worldTracker.detach(worldId));
            });
        });
        fibre.getStartupRegistry()
                .registerTypeFinder(
                        World.class,
                        Game.class,
                        world -> fibre.getGameManager().getGames().stream()
                                .filter(game -> game.get(WORLDS)
                                        .map(worldTracker -> worldTracker.contains(world))
                                        .orElse(false))
                                .findFirst());
        fibre.getStartupRegistry()
                .registerTypeFinder(
                        Entity.class,
                        Game.class,
                        entity -> fibre.getTypeResolver().find(entity.getWorld(), Game.class));
    }
}
