package cloud.emilys.fibre.api;

import cloud.emilys.fibre.api.fact.FactIndex;
import cloud.emilys.fibre.api.game.GameManager;
import cloud.emilys.fibre.api.type.TypeResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Fibre {

    FibreStartupRegistry getStartupRegistry();

    FactIndex getClassFactIndex();

    TypeResolver getTypeResolver();

    GameManager getGameManager();

    boolean isReady();

    static Fibre get() {
        RegisteredServiceProvider<Fibre> registration =
                Bukkit.getServicesManager().getRegistration(Fibre.class);
        if (registration == null) {
            throw new IllegalStateException("Fibre is not loaded");
        }
        return registration.getProvider();
    }
}
