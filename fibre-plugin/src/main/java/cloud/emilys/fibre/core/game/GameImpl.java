package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.PrimaryThreadUtil;
import cloud.emilys.fibre.api.UserFacingFibreException;
import cloud.emilys.fibre.api.data.RuntimeData;
import cloud.emilys.fibre.api.event.GamePlayerAddEvent;
import cloud.emilys.fibre.api.event.GamePlayerRemoveEvent;
import cloud.emilys.fibre.api.game.Completion;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import cloud.emilys.fibre.api.game.Players;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import cloud.emilys.fibre.core.data.RuntimeDataImpl;
import cloud.emilys.fibre.core.util.ExceptionUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class GameImpl implements Game {

    private final RuntimeData data = new RuntimeDataImpl();
    private final List<GameCompletionListener<?>> completionListeners;
    private final GameManagerImpl manager;
    private final JavaPlugin plugin;
    private final Map<UUID, Player> players = new LinkedHashMap<>();
    private final Set<PlayerJoinTokenImpl> pendingJoins = ConcurrentHashMap.newKeySet();

    private @Nullable Scope scope;
    private boolean closed;

    private GameImpl(List<GameCompletionListener<?>> completionListeners, GameManagerImpl manager, JavaPlugin plugin) {
        this.completionListeners = List.copyOf(Objects.requireNonNull(completionListeners, "completionListeners"));
        this.manager = Objects.requireNonNull(manager, "manager");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    static CompletableFuture<Game> create(
            GameManagerImpl manager,
            JavaPlugin plugin,
            Class<?> rootClass,
            List<GameCompletionListener<?>> completionListeners,
            Map<ObjectKey, Object> base) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(rootClass, "rootClass");
        Objects.requireNonNull(base, "base");

        GameImpl game = new GameImpl(completionListeners, manager, plugin);
        try {
            return ScopeFactory.asynchronous()
                    .initialize(ScopeBlueprint.builder()
                            .setGame(game)
                            .setInitialKey(ObjectKey.fromType(rootClass))
                            .setInputObjects(base)
                            .setInputObject(Players.class, new GamePlayersView(game))
                            .setInputObject(Completion.class, new CompletionImpl(game))
                            .build())
                    .thenApplyAsync(
                            scope -> {
                                scope.setName("Game " + rootClass.getSimpleName());
                                game.scope = Objects.requireNonNull(scope, "scope");
                                try {
                                    manager.addGameInstance(game);
                                } catch (RuntimeException | Error failure) {
                                    game.scope = null;
                                    scope.close();
                                    throw failure;
                                }
                                return (Game) game;
                            },
                            PrimaryThreadUtil.createExecutor(plugin))
                    .whenCompleteAsync(
                            (_, failure) -> {
                                if (failure != null) {
                                    game.handleException(failure);
                                }
                            },
                            PrimaryThreadUtil.createExecutor(plugin))
                    .toCompletableFuture();
        } catch (RuntimeException failure) {
            game.handleException(failure);
            return CompletableFuture.failedFuture(failure);
        }
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Scope getRootScope() {
        PrimaryThreadUtil.assertPrimary();
        Scope scope = this.scope;
        if (scope == null) {
            throw new IllegalStateException("Game instance is not active");
        }
        return scope;
    }

    @Override
    public PlayerJoinToken createPlayerJoinToken(UUID playerId) {
        PrimaryThreadUtil.assertPrimary();
        this.assertActive();
        return this.manager.createPlayerJoinToken(this, Objects.requireNonNull(playerId, "playerId"));
    }

    void trackPlayerJoinToken(PlayerJoinTokenImpl token) {
        this.assertActive();
        if (!this.pendingJoins.add(Objects.requireNonNull(token, "token"))) {
            throw new IllegalStateException("Player join token is already owned by this game");
        }
    }

    void releasePlayerJoinToken(PlayerJoinTokenImpl token) {
        this.pendingJoins.remove(Objects.requireNonNull(token, "token"));
    }

    void finishPlayerJoin(PlayerJoinTokenImpl token, Player player) {
        PrimaryThreadUtil.assertPrimary();
        this.assertActive();
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(player, "player");
        if (!this.pendingJoins.remove(token)) {
            throw new IllegalStateException("Player join token is not owned by this game");
        }

        UUID playerId = player.getUniqueId();
        if (this.players.putIfAbsent(playerId, player) != null) {
            throw new IllegalStateException("Player is already in this game");
        }
        try {
            Bukkit.getPluginManager().callEvent(new GamePlayerAddEvent(this, player));
        } catch (RuntimeException | Error failure) {
            this.players.remove(playerId, player);
            throw failure;
        }
    }

    @Override
    public boolean removePlayer(Player player) {
        PrimaryThreadUtil.assertPrimary();
        this.assertActive();
        Objects.requireNonNull(player, "player");
        UUID playerId = player.getUniqueId();
        Player registered = this.players.get(playerId);
        if (registered == null) {
            return false;
        }

        this.manager.removePlayer(this, registered);
        this.players.remove(playerId);
        Bukkit.getPluginManager().callEvent(new GamePlayerRemoveEvent(this, registered));
        return true;
    }

    @Override
    public List<Player> getPlayers() {
        PrimaryThreadUtil.assertPrimary();
        return List.copyOf(this.players.values());
    }

    @Override
    public void complete(Object result) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(result, "result");
        if (this.closed) {
            return;
        }
        this.beginClose();
        try {
            this.completionListeners.forEach(listener -> dispatchListener(result, listener));
        } catch (RuntimeException exception) {
            this.handleException(exception);
        } finally {
            this.closeScope();
        }
    }

    @Override
    public void handleException(Throwable exception) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(exception, "exception");

        Throwable failure = ExceptionUtil.unwrap(exception);
        if (failure instanceof UserFacingFibreException) {
            this.plugin.getLogger().severe(failure.getMessage());
        } else {
            this.plugin.getLogger().log(Level.SEVERE, "An exception caused a game to be forcibly closed", failure);
        }
        if (!this.closed) {
            try {
                this.beginClose();
            } finally {
                this.closeScope();
            }
        }
    }

    private void beginClose() {
        this.closed = true;
        for (PlayerJoinTokenImpl token : List.copyOf(this.pendingJoins)) {
            token.cancel();
        }
        for (Player player : List.copyOf(this.players.values())) {
            try {
                this.manager.removePlayer(this, player);
                this.players.remove(player.getUniqueId());
                Bukkit.getPluginManager().callEvent(new GamePlayerRemoveEvent(this, player));
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.WARNING, "An exception was thrown when closing game", exception);
            }
        }
        this.manager.removeGameInstance(this);
    }

    private void closeScope() {
        Scope scope = this.scope;
        this.scope = null;
        if (scope != null) {
            scope.close();
        }
    }

    private <T> void dispatchListener(Object result, GameCompletionListener<T> listener) {
        if (listener.type().isInstance(result)) {
            listener.consumer().accept(listener.type().cast(result));
        }
    }

    private void assertActive() {
        if (this.closed || this.scope == null) {
            throw new IllegalStateException("Game instance is not active");
        }
    }

    @Override
    public RuntimeData getRuntimeData() {
        return this.data;
    }
}
