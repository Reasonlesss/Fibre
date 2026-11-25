package codes.reason.fibre.bukkit;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.game.GameManager;
import codes.reason.fibre.plugin.PluginManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
record BukkitFibre(GameManager games, PluginManager plugins, BukkitPlatform platform) implements Fibre {
}
