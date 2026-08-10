package cloud.emilys.fibre.platform;

import cloud.emilys.fibre.api.Fibre;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FibrePlatform {

    void install(Fibre fibre);

    static FibrePlatform get() {
        RegisteredServiceProvider<FibrePlatform> registration =
                Bukkit.getServicesManager().getRegistration(FibrePlatform.class);
        if (registration == null) {
            throw new IllegalStateException("Could not find an appropriate fibre platform");
        }
        return registration.getProvider();
    }
}
