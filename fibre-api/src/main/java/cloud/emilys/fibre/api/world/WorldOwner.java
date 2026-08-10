package cloud.emilys.fibre.api.world;

import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorldOwner {

    World getWorld();
}
