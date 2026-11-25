package codes.reason.fibre.game;

import codes.reason.fibre.event.GamePlayerJoinEvent;
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

    private final Map<UUID, GameHolder> games = new LinkedHashMap<>();
    private final Map<UUID, GameHolder> gamesByPlayer = new HashMap<>();
    // each player's state from before they joined any game, given back once they're in none
    private final Map<UUID, PlayerSnapshot> outside = new HashMap<>();
    private final PluginManager plugins;
    private final PlayerStates states;
    private final GameRegionIndex regions = new GameRegionIndex();
    private final FibreEvents events = new FibreEvents();

    public GameManager(PluginManager plugins, PlayerStates states) {
        this.plugins = Objects.requireNonNull(plugins, "plugins must not be null");
        this.states = Objects.requireNonNull(states, "states must not be null");
    }

    // every game's events, with the game each came from
    public FibreEvents events() {
        return this.events;
    }

    public GameBuilder create() {
        return new GameBuilder(this);
    }

    public List<GameHolder> games() {
        return List.copyOf(this.games.values());
    }

    public Optional<GameHolder> find(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(this.games.get(id));
    }

    public Optional<GameHolder> findByPlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId must not be null");
        return Optional.ofNullable(this.gamesByPlayer.get(playerId));
    }

    public Optional<GameHolder> findByPosition(WorldPosition position) {
        Objects.requireNonNull(position, "position must not be null");
        return this.regions.find(position);
    }

    JoinResult addPlayer(GameHolder game, PlayerAdapter<?> player) {
        if (game.lifecycle().isClosed() || !this.games.containsKey(game.identity().id())) {
            throw new IllegalStateException("Can't add a player to %s as it isn't running".formatted(game));
        }
        UUID playerId = player.id();
        GameHolder previous = this.gamesByPlayer.get(playerId);
        if (previous == game) {
            return new JoinResult.AlreadyIn();
        }
        // asked before anything changes, so a denial leaves the player exactly where they were
        GamePlayerJoinEvent join = new GamePlayerJoinEvent(player);
        if (!game.events().call(join) && !join.denied()) {
            join.deny("Something went wrong while joining");
        }
        if (join.denied()) {
            return new JoinResult.Denied(join.denial().orElseThrow());
        }
        if (game.lifecycle().isClosed()) {
            return new JoinResult.Denied("The game closed");
        }
        // a join listener may have moved the player
        previous = this.gamesByPlayer.get(playerId);
        if (previous == game) {
            return new JoinResult.AlreadyIn();
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
                return new JoinResult.Redirected();
            }
        }
        // before the add events, so listeners see the player as this game left them, or fresh, and in its space
        boolean restored = game.snapshots().apply(player);
        // a returning player stays where they left, unless that's gone or outside the game
        if (!restored || !game.contains(player.position())) {
            this.states.place(player, game.spawn(player));
        }
        game.players().add(player);
        return new JoinResult.Joined();
    }

    boolean removePlayer(GameHolder game, UUID playerId) {
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
    private @Nullable PlayerAdapter<?> leave(GameHolder game, UUID playerId) {
        PlayerAdapter<?> player = game.players().get(playerId).orElse(null);
        if (player == null) {
            return null;
        }
        game.snapshots().save(player);
        game.players().remove(playerId);
        return player;
    }

    void release(GameHolder game) {
        for (PlayerAdapter<?> player : game.players().all()) {
            this.removePlayer(game, player.id());
        }
    }

    PlayerStates states() {
        return this.states;
    }

    PluginManager plugins() {
        return this.plugins;
    }

    void ensureNoOverlap(GameIdentity identity, List<GameRegion> regions) {
        for (GameRegion region : regions) {
            this.regions.ensureNoOverlap(identity, region);
        }
    }

    void register(GameHolder game) {
        UUID id = game.identity().id();
        if (this.games.containsKey(id)) {
            throw new IllegalStateException("A game with id %s is already registered".formatted(id));
        }
        this.regions.add(game);
        this.games.put(id, game);
    }

    void unregister(UUID id) {
        GameHolder game = this.games.remove(id);
        if (game != null) {
            this.gamesByPlayer.values().removeIf(value -> value == game);
            this.regions.remove(game);
        }
    }

}
