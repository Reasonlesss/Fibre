package cloud.emilys.fibre.core;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.FibreStartupRegistry;
import cloud.emilys.fibre.api.fact.FactIndex;
import cloud.emilys.fibre.api.game.GameManager;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import cloud.emilys.fibre.api.game.PlayerPreloader;
import cloud.emilys.fibre.api.scope.discovery.ScopeContributor;
import cloud.emilys.fibre.api.scope.lifecycle.ScopeInitializer;
import cloud.emilys.fibre.api.type.TypeResolver;
import cloud.emilys.fibre.core.fact.FactIndexImpl;
import cloud.emilys.fibre.core.game.GameManagerImpl;
import cloud.emilys.fibre.core.type.TypeResolverImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibreImpl implements Fibre {

    private final FibreStartupRegistryImpl registry = new FibreStartupRegistryImpl();
    private final GameManagerImpl gameManager;
    private List<Consumer<PlayerJoinToken>> playerJoinInitializers = new ArrayList<>();
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

    public List<PlayerPreloader> getPlayerPreloaders() {
        if (!this.ready) {
            throw new IllegalStateException("getPlayerPreloaders must be called after Fibre has been enabled.");
        }
        return this.registry.getPlayerPreloaders();
    }

    public void registerPlayerJoinInitializer(Consumer<PlayerJoinToken> initializer) {
        if (this.ready) {
            throw new IllegalStateException("Player join initializers must be registered before Fibre is enabled.");
        }
        this.playerJoinInitializers.add(Objects.requireNonNull(initializer, "initializer"));
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
        this.playerJoinInitializers = List.copyOf(this.playerJoinInitializers);
        this.factIndex = new FactIndexImpl(this.registry.getFactScanners());
        this.typeResolver = new TypeResolverImpl(this.registry.getTypeFinders());
        this.ready = true;
    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    private void initializePlayerJoinToken(PlayerJoinToken token) {
        for (Consumer<PlayerJoinToken> initializer : this.playerJoinInitializers) {
            initializer.accept(token);
        }
    }
}
