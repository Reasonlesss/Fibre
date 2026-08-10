package cloud.emilys.fibre;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.FibreBridge;
import cloud.emilys.fibre.core.FibreBridgeImpl;
import cloud.emilys.fibre.core.FibreImpl;
import cloud.emilys.fibre.features.container.ContainerFeature;
import cloud.emilys.fibre.features.dependency.DependencyFeature;
import cloud.emilys.fibre.features.event.EventFeature;
import cloud.emilys.fibre.features.lifecycle.LifecycleFeature;
import cloud.emilys.fibre.features.state.StateFeature;
import cloud.emilys.fibre.features.world.WorldFeature;
import cloud.emilys.fibre.platform.FibrePlatform;
import cloud.emilys.fibre.platform.FibrePlatformFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibrePlugin extends JavaPlugin implements Listener {

    private @Nullable FibreImpl fibre;

    @Override
    public void onLoad() {
        // Setup internal fibre features
        this.fibre = new FibreImpl();
        FibreBridgeImpl bridge = new FibreBridgeImpl();
        FibrePlatform platform = FibrePlatformFactory.create();

        // Register services with Bukkit's services manager
        this.getServer().getServicesManager().register(FibreBridge.class, bridge, this, ServicePriority.Normal);
        this.getServer().getServicesManager().register(FibrePlatform.class, platform, this, ServicePriority.Normal);
        this.getServer().getServicesManager().register(Fibre.class, this.fibre, this, ServicePriority.Normal);

        // Install builtin features
        ContainerFeature.install(this.fibre);
        EventFeature.install(this.fibre);
        DependencyFeature.install(this.fibre);
        LifecycleFeature.install(this.fibre);
        StateFeature.install(this.fibre);
        WorldFeature.install(this.fibre);
        platform.install(this.fibre);
    }

    @Override
    public void onEnable() {
        assert this.fibre != null;
        this.fibre.finishSetup();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        assert this.fibre != null;
        this.fibre.getGameManager().findGame(event.getPlayer()).ifPresent(game -> game.removePlayer(event.getPlayer()));
    }
}
