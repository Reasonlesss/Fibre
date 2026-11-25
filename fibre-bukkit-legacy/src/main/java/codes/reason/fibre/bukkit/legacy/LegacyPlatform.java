package codes.reason.fibre.bukkit.legacy;

import codes.reason.fibre.bukkit.BukkitPlatform;
import codes.reason.fibre.bukkit.Respawns;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LegacyPlatform {

    private LegacyPlatform() {}

    public static BukkitPlatform create(JavaPlugin plugin) {
        Respawns respawns = new Respawns(plugin);
        return new BukkitPlatform(plugin, new LegacyEventLookup(), new LegacyPlayerStates(respawns), respawns, _ -> 0, playersAreAudiences());
    }

    // Spigot 1.8 has no Adventure, but some forks ship it and make Player an Audience
    private static boolean playersAreAudiences() {
        try {
            return Class.forName("net.kyori.adventure.audience.Audience", false, Player.class.getClassLoader())
                    .isAssignableFrom(Player.class);
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

}
