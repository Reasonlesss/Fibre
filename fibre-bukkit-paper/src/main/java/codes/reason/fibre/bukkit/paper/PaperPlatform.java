package codes.reason.fibre.bukkit.paper;

import codes.reason.fibre.bukkit.BukkitPlatform;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PaperPlatform {

    private PaperPlatform() {}

    public static BukkitPlatform create(JavaPlugin plugin) {
        return new BukkitPlatform(plugin, new PaperEventLookup(), new PaperPlayerStates(), World::getMinHeight, true);
    }

}
