package codes.reason.fibre.world;

import codes.reason.fibre.player.PlayerAdapter;
import org.jspecify.annotations.NullMarked;

// where a game puts a player who joins it fresh, or respawns in it; bound in the game's scope like its GameRegion.
// Without one, players go to the world spawn if it's in the game's region, else the region's centre.
@NullMarked
@FunctionalInterface
public interface GameSpawn {

    WorldPosition spawn(PlayerAdapter<?> player);

}
