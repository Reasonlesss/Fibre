package codes.reason.fibre.game;

import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.world.GameRegion;
import codes.reason.fibre.scope.Scope;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Game {

    private final GameManager manager;
    private final GameIdentity identity;
    private final Scope scope;
    private final GameExecutor executor;
    private final GameLifecycle lifecycle;
    private final GameEvents events;
    private final GamePlayers players;
    private final GameSnapshots snapshots;
    private final List<GameRegion> regions;

    Game(Scope scope, GameManager manager) {
        this.scope = scope;
        this.manager = manager;
        this.identity = scope.require(GameIdentity.class);
        this.executor = scope.require(GameExecutor.class);
        this.lifecycle = scope.require(GameLifecycle.class);
        this.events = scope.require(GameEvents.class);
        this.players = scope.require(GamePlayers.class);
        this.snapshots = scope.require(GameSnapshots.class);
        this.regions = scope.list(GameRegion.class);
    }

    public GameIdentity identity() {
        return this.identity;
    }

    public Scope scope() {
        return this.scope;
    }

    public GameExecutor executor() {
        return this.executor;
    }

    public GameLifecycle lifecycle() {
        return this.lifecycle;
    }

    public GameEvents events() {
        return this.events;
    }

    public GamePlayers players() {
        return this.players;
    }

    public GameSnapshots snapshots() {
        return this.snapshots;
    }

    public List<GameRegion> regions() {
        return this.regions;
    }

    public JoinResult addPlayer(PlayerAdapter<?> player) {
        Objects.requireNonNull(player, "player must not be null");
        return this.manager.addPlayer(this, player);
    }

    public boolean removePlayer(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return this.manager.removePlayer(this, id);
    }

    public boolean removePlayer(PlayerAdapter<?> player) {
        Objects.requireNonNull(player, "player must not be null");
        return this.removePlayer(player.id());
    }

    @Override
    public String toString() {
        return "Game " + this.identity;
    }

}
