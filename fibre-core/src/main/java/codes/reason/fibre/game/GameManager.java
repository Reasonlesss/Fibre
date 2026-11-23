package codes.reason.fibre.game;

import codes.reason.fibre.platform.PlayerStates;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.player.PlayerSnapshot;
import codes.reason.fibre.plugin.PluginManager;
import codes.reason.fibre.world.GameRegion;
import codes.reason.fibre.world.WorldPosition;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GameManager {

    private final Map<UUID, Game> games = new LinkedHashMap<>();
    private final Map<UUID, Game> gamesByPlayer = new HashMap<>();
    // each player's state from before they joined any game, given back once they're in none
    private final Map<UUID, PlayerSnapshot> outside = new HashMap<>();
    // each game's state of the players who left it, given back if they rejoin
    private final Map<Game, Map<UUID, PlayerSnapshot>> left = new HashMap<>();
    private final PluginManager plugins;
    private final PlayerStates states;
    private final GameRegionIndex regions = new GameRegionIndex();

    public GameManager(PluginManager plugins, PlayerStates states) {
        this.plugins = Objects.requireNonNull(plugins, "plugins must not be null");
        this.states = Objects.requireNonNull(states, "states must not be null");
    }

    public GameBuilder create() {
        return new GameBuilder(this);
    }

    public List<Game> games() {
        return List.copyOf(this.games.values());
    }

    public Optional<Game> find(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(this.games.get(id));
    }

    public Optional<Game> findByPlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId must not be null");
        return Optional.ofNullable(this.gamesByPlayer.get(playerId));
    }

    public Optional<Game> findByPosition(WorldPosition position) {
        Objects.requireNonNull(position, "position must not be null");
        return this.regions.find(position);
    }

    boolean addPlayer(Game game, PlayerAdapter<?> player) {
        if (game.lifecycle().isClosed() || !this.games.containsKey(game.identity().id())) {
            throw new IllegalStateException("Can't add a player to %s as it isn't running".formatted(game));
        }
        UUID playerId = player.id();
        Game previous = this.gamesByPlayer.get(playerId);
        if (previous == game) {
            return false;
        }
        if (previous == null && !this.outside.containsKey(playerId)) {
            this.outside.put(playerId, this.states.capture(player));
        }
        // claimed before any event fires, so a listener moving the player sees where they're going
        this.gamesByPlayer.put(playerId, game);
        if (previous != null) {
            this.leave(previous, playerId);
            // a remove listener moved the player somewhere else, and that move wins
            if (this.gamesByPlayer.get(playerId) != game) {
                return false;
            }
        }
        // before the add events, so listeners see the player as this game left them, or fresh
        Map<UUID, PlayerSnapshot> left = this.left.get(game);
        PlayerSnapshot snapshot = left != null ? left.remove(playerId) : null;
        if (snapshot != null) {
            this.states.restore(player, snapshot);
        } else {
            this.states.reset(player);
        }
        game.players().add(player);
        return true;
    }

    boolean removePlayer(Game game, UUID playerId) {
        if (!this.gamesByPlayer.remove(playerId, game)) {
            return false;
        }
        PlayerAdapter<?> player = this.leave(game, playerId);
        // a remove listener may have put them into another game, which keeps the outside state for later
        if (player != null && !this.gamesByPlayer.containsKey(playerId)) {
            PlayerSnapshot snapshot = this.outside.remove(playerId);
            if (snapshot != null) {
                this.states.restore(player, snapshot);
            }
        }
        return true;
    }

    // saved before the remove events, so it's the player as the game had them
    private @Nullable PlayerAdapter<?> leave(Game game, UUID playerId) {
        PlayerAdapter<?> player = game.players().get(playerId).orElse(null);
        if (player == null) {
            return null;
        }
        this.left.computeIfAbsent(game, _ -> new HashMap<>()).put(playerId, this.states.capture(player));
        game.players().remove(playerId);
        return player;
    }

    void release(Game game) {
        for (PlayerAdapter<?> player : game.players().all()) {
            this.removePlayer(game, player.id());
        }
    }

    PluginManager plugins() {
        return this.plugins;
    }

    void ensureNoOverlap(GameIdentity identity, List<GameRegion> regions) {
        for (GameRegion region : regions) {
            this.regions.ensureNoOverlap(identity, region);
        }
    }

    void register(Game game) {
        UUID id = game.identity().id();
        if (this.games.containsKey(id)) {
            throw new IllegalStateException("A game with id %s is already registered".formatted(id));
        }
        this.regions.add(game);
        this.games.put(id, game);
    }

    void unregister(UUID id) {
        Game game = this.games.remove(id);
        if (game != null) {
            this.gamesByPlayer.values().removeIf(value -> value == game);
            this.left.remove(game);
            this.regions.remove(game);
        }
    }

}
