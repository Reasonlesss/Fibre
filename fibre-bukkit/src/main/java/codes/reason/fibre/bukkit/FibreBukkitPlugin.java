package codes.reason.fibre.bukkit;

import codes.reason.fibre.FibreHolder;
import codes.reason.fibre.builtin.audience.AudiencePlugin;
import codes.reason.fibre.builtin.event.EventPlugin;
import codes.reason.fibre.builtin.filter.FilterPlugin;
import codes.reason.fibre.builtin.tick.TickPlugin;
import codes.reason.fibre.game.Game;
import codes.reason.fibre.game.GameManager;
import codes.reason.fibre.platform.FibrePlatform;
import codes.reason.fibre.plugin.PluginManager;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibreBukkitPlugin extends JavaPlugin {

    private @Nullable BukkitFibre fibre;

    @Override
    public void onLoad() {
        FibrePlatform platform = this.platform(isPaper()
                ? "codes.reason.fibre.bukkit.paper.PaperPlatform"
                : "codes.reason.fibre.bukkit.legacy.LegacyPlatform");

        PluginManager plugins = new PluginManager();
        plugins.install(new TickPlugin());
        plugins.install(new FilterPlugin());
        plugins.install(new EventPlugin());
        if (hasAdventure()) {
            installAudience(plugins);
        }

        this.fibre = new BukkitFibre(new GameManager(plugins, platform.playerStates()), plugins, platform);
        FibreHolder.set(this.fibre);
    }

    @Override
    public void onEnable() {
        BukkitFibre fibre = this.fibre;
        if (fibre != null) {
            this.getServer().getPluginManager().registerEvents(new QuitListener(fibre.games()), this);
        }
    }

    @Override
    public void onDisable() {
        BukkitFibre fibre = this.fibre;
        if (fibre == null) {
            return;
        }
        for (Game game : fibre.games().games()) {
            try {
                game.lifecycle().close();
            } catch (RuntimeException exception) {
                this.getLogger().log(Level.SEVERE, "Failed to close %s".formatted(game), exception);
            }
        }
        FibreHolder.clear();
        this.fibre = null;
    }

    // only on Paper versions as new as the API fibre-bukkit-paper is built against
    private static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.connection.PlayerGameConnection", false, FibreBukkitPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    // looked up by name, since the platform modules are built on this one
    private FibrePlatform platform(String factory) {
        try {
            return (FibrePlatform) Class.forName(factory).getMethod("create", JavaPlugin.class).invoke(null, this);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Couldn't create the platform from %s".formatted(factory), exception);
        }
    }

    // players are only audiences on servers that ship Adventure, e.g. Paper or some 1.8 forks
    private static boolean hasAdventure() {
        try {
            return Class.forName("net.kyori.adventure.audience.Audience").isAssignableFrom(Player.class);
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    // kept apart so AudiencePlugin is only loaded when Adventure is there
    private static void installAudience(PluginManager plugins) {
        plugins.install(new AudiencePlugin());
    }

}
