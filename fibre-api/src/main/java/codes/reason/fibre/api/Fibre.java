package codes.reason.fibre.api;

import codes.reason.fibre.api.fact.FactIndex;
import codes.reason.fibre.api.game.GameManager;
import codes.reason.fibre.api.type.TypeResolver;
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
