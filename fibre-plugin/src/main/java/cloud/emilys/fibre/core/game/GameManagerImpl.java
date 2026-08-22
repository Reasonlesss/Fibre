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
    private final Map<UUID, PlayerJoinTokenImpl> pendingJoins = new ConcurrentHashMap<>();
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
            this.pendingJoins.entrySet().removeIf(entry -> entry.getValue().getGame() == instance);
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
        return Optional.ofNullable(this.pendingJoins.get(playerId));
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
            return !this.gamesByPlayer.containsKey(playerId) && this.pendingJoins.putIfAbsent(playerId, token) == null;
        }
    }

    void releasePlayerJoinToken(PlayerJoinTokenImpl token) {
        Objects.requireNonNull(token, "token");
        synchronized (this.membershipLock) {
            this.pendingJoins.remove(token.getPlayerId(), token);
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
            if (!this.pendingJoins.remove(playerId, token)) {
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
}
