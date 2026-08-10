package cloud.emilys.fibre.core;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.FibreStartupRegistry;
import cloud.emilys.fibre.api.fact.FactIndex;
import cloud.emilys.fibre.api.game.GameManager;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import cloud.emilys.fibre.api.scope.lifecycle.ScopeInitializer;
import cloud.emilys.fibre.api.type.TypeResolver;
import cloud.emilys.fibre.core.fact.FactIndexImpl;
import cloud.emilys.fibre.core.game.GameManagerImpl;
import cloud.emilys.fibre.core.type.TypeResolverImpl;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibreImpl implements Fibre {

    private final FibreStartupRegistryImpl registry = new FibreStartupRegistryImpl();
    private final GameManager gameManager = new GameManagerImpl(this);
    private @Nullable FactIndex factIndex;
    private @Nullable TypeResolver typeResolver;
    private boolean ready;

    @Override
    public FibreStartupRegistry getStartupRegistry() {
        return this.registry;
    }

    @Override
    public FactIndex getClassFactIndex() {
        if (!this.ready) {
            throw new IllegalStateException("getClassFactIndex must be called after Fibre has been enabled.");
        }
        assert this.factIndex != null;
        return this.factIndex;
    }

    public List<ScopeContributor> getScopeContributors() {
        if (!this.ready) {
            throw new IllegalStateException("getScopeContributors must be called after Fibre has been enabled.");
        }
        return this.registry.getScopeContributors();
    }

    public List<ScopeInitializer> getScopeInitializers() {
        if (!this.ready) {
            throw new IllegalStateException("getScopeInitializers must be called after Fibre has been enabled.");
        }
        return this.registry.getScopeInitializers();
    }

    @Override
    public TypeResolver getTypeResolver() {
        if (!this.ready) {
            throw new IllegalStateException("getTypeResolver must be called after Fibre has been enabled.");
        }
        assert this.typeResolver != null;
        return this.typeResolver;
    }

    @Override
    public GameManager getGameManager() {
        return this.gameManager;
    }

    public void finishSetup() {
        this.registry.freeze();
        this.factIndex = new FactIndexImpl(this.registry.getFactScanners());
        this.typeResolver = new TypeResolverImpl(this.registry.getTypeFinders());
        this.ready = true;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }
}
