package cloud.emilys.fibre.api;

import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import java.util.concurrent.CompletionStage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FibreBridge {

    DependencySet createDependencySet();

    ScopeBlueprint.Builder createScopeBlueprintBuilder();

    ScopeFactory<Scope> createSynchronousScopeFactory();

    ScopeFactory<CompletionStage<Scope>> createAsynchronousScopeFactory();

    static FibreBridge get() {
        RegisteredServiceProvider<FibreBridge> registration =
                Bukkit.getServicesManager().getRegistration(FibreBridge.class);
        if (registration == null) {
            throw new IllegalStateException("The Fibre bridge is not loaded");
        }
        return registration.getProvider();
    }
}
