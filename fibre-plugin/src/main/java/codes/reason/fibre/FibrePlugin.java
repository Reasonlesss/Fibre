package codes.reason.fibre;

import codes.reason.fibre.api.Fibre;
import codes.reason.fibre.api.FibreBridge;
import codes.reason.fibre.core.FibreBridgeImpl;
import codes.reason.fibre.core.FibreImpl;
import codes.reason.fibre.features.container.ContainerFeature;
import codes.reason.fibre.features.dependency.DependencyFeature;
import codes.reason.fibre.features.event.EventFeature;
import codes.reason.fibre.features.lifecycle.LifecycleFeature;
import codes.reason.fibre.features.playerpreload.PlayerPreloadFeature;
import codes.reason.fibre.features.state.StateFeature;
import codes.reason.fibre.features.world.WorldFeature;
import codes.reason.fibre.platform.FibrePlatform;
import codes.reason.fibre.platform.FibrePlatformFactory;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibrePlugin extends JavaPlugin {

    private @Nullable FibreImpl fibre;

    @Override
    public void onLoad() {
        // Setup internal fibre features
        this.fibre = new FibreImpl(this);
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
        PlayerPreloadFeature.install(this.fibre, this);
        platform.install(this.fibre);
    }

    @Override
    public void onEnable() {
        assert this.fibre != null;
        this.fibre.finishSetup();
    }
}
