package codes.reason.fibre.bukkit;

import codes.reason.fibre.FibreHolder;
import codes.reason.fibre.builtin.audience.AudiencePlugin;
import codes.reason.fibre.game.Game;
import codes.reason.fibre.game.GameManager;
import codes.reason.fibre.plugin.PluginManager;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibreBukkitPlugin extends JavaPlugin {

    private @Nullable BukkitFibre fibre;

    @Override
    public void onLoad() {
        BukkitPlatform platform = this.platform(isPaper()
                ? "codes.reason.fibre.bukkit.paper.PaperPlatform"
                : "codes.reason.fibre.bukkit.legacy.LegacyPlatform");

        PluginManager plugins = new PluginManager();
        if (platform.audiences()) {
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
    private BukkitPlatform platform(String factory) {
        try {
            return (BukkitPlatform) Class.forName(factory).getMethod("create", JavaPlugin.class).invoke(null, this);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Couldn't create the platform from %s".formatted(factory), exception);
        }
    }

    // kept apart so AudiencePlugin is only loaded when Adventure is there
    private static void installAudience(PluginManager plugins) {
        plugins.install(new AudiencePlugin());
    }

}
