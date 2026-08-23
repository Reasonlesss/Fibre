package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.PrimaryThreadUtil;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.GameBuilder;
import cloud.emilys.fibre.api.game.GameManager;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GameManagerImpl implements GameManager {

    private final Fibre api;
    private final Consumer<PlayerJoinToken> tokenInitializer;
    private final Object membershipLock = new Object();
    private final Set<GameImpl> instances = new LinkedHashSet<>();
    // Unpublished reservations block duplicates without exposing a token before its initializers finish.
    private final Map<UUID, PlayerJoinReservation> pendingJoins = new ConcurrentHashMap<>();
    private final Map<UUID, GameImpl> gamesByPlayer = new ConcurrentHashMap<>();

    public GameManagerImpl(Fibre api, Consumer<PlayerJoinToken> tokenInitializer) {
        this.api = Objects.requireNonNull(api, "api");
        this.tokenInitializer = Objects.requireNonNull(tokenInitializer, "tokenInitializer");
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

    PlayerJoinTokenImpl createPlayerJoinToken(GameImpl game, UUID playerId) {
        PrimaryThreadUtil.assertPrimary();
        this.assertRegistered(game);
        Objects.requireNonNull(playerId, "playerId");

        PlayerJoinTokenImpl token = new PlayerJoinTokenImpl(game, playerId, this::releaseToken, this::joinToken);
        if (!this.reservePlayerJoinToken(token)) {
            throw new IllegalStateException("Player already has a pending token or active game");
        }

        try {
            game.trackPlayerJoinToken(token);
            this.tokenInitializer.accept(token);
            this.publishPlayerJoinToken(token);
            return token;
        } catch (RuntimeException | Error failure) {
            token.cancel();
            throw failure;
        }
    }

    boolean reservePlayerJoinToken(PlayerJoinTokenImpl token) {
        Objects.requireNonNull(token, "token");
        synchronized (this.membershipLock) {
            UUID playerId = token.getPlayerId();
            return !this.gamesByPlayer.containsKey(playerId)
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

    void releasePlayerJoinToken(PlayerJoinTokenImpl token) {
        Objects.requireNonNull(token, "token");
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
        synchronized (this.membershipLock) {
            PlayerJoinReservation reservation = this.pendingJoins.get(playerId);
            if (reservation == null
                    || reservation.token() != token
                    || !reservation.isPublished()
                    || !this.pendingJoins.remove(playerId, reservation)) {
                throw new IllegalStateException("Player join token is no longer registered");
            }
            if (this.gamesByPlayer.putIfAbsent(playerId, game) != null) {
                throw new IllegalStateException("Player already belongs to a game");
            }
        }

        try {
            game.finishPlayerJoin(token, player);
        } catch (RuntimeException | Error failure) {
            synchronized (this.membershipLock) {
                this.gamesByPlayer.remove(playerId, game);
            }
            game.releasePlayerJoinToken(token);
            throw failure;
        }
    }

    private void releaseToken(PlayerJoinTokenImpl token) {
        this.releasePlayerJoinToken(token);
        ((GameImpl) token.getGame()).releasePlayerJoinToken(token);
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
