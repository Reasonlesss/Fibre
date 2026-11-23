package codes.reason.fibre.bukkit.legacy;

import codes.reason.fibre.bukkit.BukkitPlatform;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LegacyPlatform {

    private LegacyPlatform() {}

    public static BukkitPlatform create(JavaPlugin plugin) {
        return new BukkitPlatform(plugin, new LegacyEventLookup(), new LegacyPlayerStates(), _ -> 0);
    }

}
