package codes.reason.fibre.core;

import codes.reason.fibre.api.Fibre;
import codes.reason.fibre.api.FibreStartupRegistry;
import codes.reason.fibre.api.fact.FactIndex;
import codes.reason.fibre.api.game.GameManager;
import codes.reason.fibre.api.game.PlayerJoinToken;
import codes.reason.fibre.api.game.PlayerPreloader;
import codes.reason.fibre.api.scope.discovery.ScopeContributor;
import codes.reason.fibre.api.scope.lifecycle.ScopeInitializer;
import codes.reason.fibre.api.type.TypeResolver;
import codes.reason.fibre.core.fact.FactIndexImpl;
import codes.reason.fibre.core.game.GameManagerImpl;
import codes.reason.fibre.core.type.TypeResolverImpl;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibreImpl implements Fibre {

    private final FibreStartupRegistryImpl registry = new FibreStartupRegistryImpl();
    private final GameManagerImpl gameManager;
    private @Nullable FactIndex factIndex;
    private @Nullable TypeResolver typeResolver;
    private boolean ready;

    public FibreImpl() {
        this.gameManager = new GameManagerImpl(this, this::initializePlayerJoinToken);
    }

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

    private void initializePlayerJoinToken(PlayerJoinToken token) {
        for (PlayerPreloader preloader : this.registry.getPlayerPreloaders()) {
            token.waitFor(
                    Objects.requireNonNull(preloader.preload(token.getPlayerId()), "Player preloader returned null"));
        }
    }
}
