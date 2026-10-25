package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.PrimaryThreadUtil;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.GameBuilder;
import cloud.emilys.fibre.api.game.GameManager;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import cloud.emilys.fibre.api.game.PlayerPreloader;
import cloud.emilys.fibre.api.game.PreloadSink;
import cloud.emilys.fibre.core.util.ExceptionUtil;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GameManagerImpl implements GameManager, Listener {

    private final Fibre api;
    private final Supplier<List<PlayerPreloader>> playerPreloaders;
    private final JavaPlugin plugin;
    private final Object membershipLock = new Object();
    private final Set<GameImpl> instances = new LinkedHashSet<>();
    // Unpublished reservations block duplicates without exposing a token before its initializers finish.
    private final Map<UUID, PlayerJoinReservation> pendingJoins = new ConcurrentHashMap<>();
    private final Map<UUID, GameImpl> gamesByPlayer = new ConcurrentHashMap<>();

    public GameManagerImpl(Fibre api, Supplier<List<PlayerPreloader>> playerPreloaders, JavaPlugin plugin) {
        this.api = Objects.requireNonNull(api, "api");
        this.playerPreloaders = Objects.requireNonNull(playerPreloaders, "playerPreloaders");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public GameBuilder createGame() {
        PrimaryThreadUtil.assertPrimary();
        return new GameBuilderImpl(this);
    }

    void addGameInstance(GameImpl instance) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(instance, "instance");
        if (!this.instances.add(instance)) {
            throw new IllegalStateException("Game instance is already registered");
        }
    }

    void removeGameInstance(GameImpl instance) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(instance, "instance");
        if (!this.instances.remove(instance)) {
            return;
        }
        synchronized (this.membershipLock) {
            this.gamesByPlayer.entrySet().removeIf(entry -> entry.getValue() == instance);
            this.pendingJoins
                    .entrySet()
                    .removeIf(entry -> entry.getValue().token().getGame() == instance);
        }
    }

    @Override
    public List<Game> getGames() {
        PrimaryThreadUtil.assertPrimary();
        return List.copyOf(this.instances);
    }

    @Override
    public Optional<PlayerJoinToken> findPlayerJoinToken(UUID playerId) {
        PrimaryThreadUtil.assertPrimary();
        return this.findToken(playerId).filter(PlayerJoinTokenImpl::isPending).map(PlayerJoinToken.class::cast);
    }

    private Optional<PlayerJoinTokenImpl> findToken(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        PlayerJoinReservation reservation = this.pendingJoins.get(playerId);
        if (reservation == null || !reservation.isPublished()) {
            return Optional.empty();
        }
        return Optional.of(reservation.token());
    }

    @Override
    public Optional<Game> findGame(Object object) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(object, "object");

        if (object instanceof Player player) {
            return Optional.ofNullable(this.gamesByPlayer.get(player.getUniqueId()));
        }

        if (object instanceof GameImpl game) {
            return this.instances.contains(game) ? Optional.of(game) : Optional.empty();
        }

        return this.api.getTypeResolver().find(object, Game.class).filter(this::isRegistered);
    }

    PlayerJoinTokenImpl addPlayer(GameImpl game, UUID playerId, Consumer<? super PreloadSink> preload) {
        PrimaryThreadUtil.assertPrimary();
        this.assertRegistered(game);
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(preload, "preload");

        PlayerJoinTokenImpl token = new PlayerJoinTokenImpl(game, this, playerId);
        if (!this.reservePlayerJoinToken(token)) {
            throw new IllegalStateException("Player already has a pending join");
        }

        try {
            game.trackPlayerJoinToken(token);
            for (PlayerPreloader preloader : this.playerPreloaders.get()) {
                preloader.preload(game, playerId, token.getSink());
            }
            preload.accept(token.getSink());
            this.publishPlayerJoinToken(token);
        } catch (RuntimeException | Error failure) {
            token.cancel();
            throw failure;
        }
        // Sealed after publishing so a preload that has already failed leaves a failed token rather than throwing.
        token.seal();
        return token;
    }

    CompletionStage<Player> commit(PlayerJoinTokenImpl token) {
        PrimaryThreadUtil.assertPrimary();
        return token.ready()
                .thenComposeAsync(
                        _ -> {
                            if (token.isPending()) {
                                Player player = Bukkit.getPlayer(token.getPlayerId());
                                if (player == null || !player.isOnline()) {
                                    throw new IllegalStateException("Player is not online");
                                }
                                this.joinToken(token, player);
                            }
                            return token.committed();
                        },
                        PrimaryThreadUtil.createExecutor(this.plugin));
    }

    boolean reservePlayerJoinToken(PlayerJoinTokenImpl token) {
        Objects.requireNonNull(token, "token");
        synchronized (this.membershipLock) {
            UUID playerId = token.getPlayerId();
            return this.gamesByPlayer.get(playerId) != token.getGame()
                    && this.pendingJoins.putIfAbsent(playerId, new PlayerJoinReservation(token)) == null;
        }
    }

    void publishPlayerJoinToken(PlayerJoinTokenImpl token) {
        Objects.requireNonNull(token, "token");
        synchronized (this.membershipLock) {
            PlayerJoinReservation reservation = this.pendingJoins.get(token.getPlayerId());
            if (reservation == null || reservation.token() != token) {
                throw new IllegalStateException("Player join token is no longer registered");
            }
            reservation.publish();
        }
    }

    void releaseToken(PlayerJoinTokenImpl token) {
        Objects.requireNonNull(token, "token");
        ((GameImpl) token.getGame()).releasePlayerJoinToken(token);
        synchronized (this.membershipLock) {
            PlayerJoinReservation reservation = this.pendingJoins.get(token.getPlayerId());
            if (reservation != null && reservation.token() == token) {
                this.pendingJoins.remove(token.getPlayerId(), reservation);
            }
        }
    }

    void removePlayer(GameImpl game, Player player) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(player, "player");
        UUID playerId = player.getUniqueId();
        synchronized (this.membershipLock) {
            if (!this.gamesByPlayer.remove(playerId, game)) {
                throw new IllegalStateException("Player is not in this game");
            }
        }
    }

    private void joinToken(PlayerJoinTokenImpl token, Player player) {
        PrimaryThreadUtil.assertPrimary();
        GameImpl game = (GameImpl) token.getGame();
        UUID playerId = token.getPlayerId();
        token.beginJoin(player);

        GameImpl previous;
        synchronized (this.membershipLock) {
            PlayerJoinReservation reservation = this.pendingJoins.get(playerId);
            if (reservation == null
                    || reservation.token() != token
                    || !reservation.isPublished()
                    || !this.pendingJoins.remove(playerId, reservation)) {
                throw new IllegalStateException("Player join token is no longer registered");
            }
            previous = this.gamesByPlayer.get(playerId);
        }

        try {
            if (previous != null) {
                previous.removePlayer(player);
            }
            synchronized (this.membershipLock) {
                if (this.gamesByPlayer.putIfAbsent(playerId, game) != null) {
                    throw new IllegalStateException("Player already belongs to a game");
                }
            }
            game.finishPlayerJoin(token, player);
        } catch (RuntimeException | Error failure) {
            synchronized (this.membershipLock) {
                this.gamesByPlayer.remove(playerId, game);
            }
            token.fail(failure);
            throw failure;
        }
        token.joined(player);
    }

    void joinWhenReady(PlayerJoinTokenImpl token) {
        token.ready()
                .thenRunAsync(
                        () -> {
                            Player player = Bukkit.getPlayer(token.getPlayerId());
                            if (player != null
                                    && player.isOnline()
                                    && token.isPending()
                                    && this.findToken(token.getPlayerId()).orElse(null) == token) {
                                this.joinToken(token, player);
                            }
                        },
                        PrimaryThreadUtil.createExecutor(this.plugin));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerId = event.getUniqueId();
        try {
            this.findToken(playerId)
                    .filter(PlayerJoinTokenImpl::isDeferringLogin)
                    .ifPresent(token -> token.ready().toCompletableFuture().join());
        } catch (CompletionException | CancellationException failure) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Your game data could not be prepared. Please try again.");
            if (!(ExceptionUtil.unwrap(failure) instanceof CancellationException)) {
                this.plugin
                        .getLogger()
                        .log(Level.SEVERE, "Could not prepare game data for player " + playerId, failure);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.findToken(event.getPlayer().getUniqueId())
                .filter(PlayerJoinTokenImpl::isAutoCommit)
                .ifPresent(this::joinWhenReady);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GameImpl game = this.gamesByPlayer.get(event.getPlayer().getUniqueId());
        if (game != null) {
            game.removePlayer(event.getPlayer());
        }
    }

    private boolean isRegistered(Game game) {
        return game instanceof GameImpl implementation && this.instances.contains(implementation);
    }

    private void assertRegistered(GameImpl game) {
        Objects.requireNonNull(game, "game");
        if (!this.instances.contains(game)) {
            throw new IllegalStateException("Game instance is not active");
        }
    }

    private static final class PlayerJoinReservation {

        private final PlayerJoinTokenImpl token;
        private volatile boolean published;

        private PlayerJoinReservation(PlayerJoinTokenImpl token) {
            this.token = Objects.requireNonNull(token, "token");
        }

        private PlayerJoinTokenImpl token() {
            return this.token;
        }

        private boolean isPublished() {
            return this.published;
        }

        private void publish() {
            this.published = true;
        }
    }
}
