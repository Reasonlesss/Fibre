package codes.reason.fibre.game;

import codes.reason.fibre.platform.PlayerStates;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.player.PlayerSnapshot;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

// the state of players who left this game, given back if they rejoin
@NullMarked
public final class GameSnapshots {

    private final PlayerStates states;
    private final Map<UUID, PlayerSnapshot> left = new HashMap<>();

    GameSnapshots(PlayerStates states) {
        this.states = states;
    }

    public boolean has(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return this.left.containsKey(id);
    }

    void save(PlayerAdapter<?> player) {
        this.left.put(player.id(), this.states.capture(player));
    }

    // the player as this game left them, or fresh if they've never played it
    void apply(PlayerAdapter<?> player) {
        PlayerSnapshot snapshot = this.left.remove(player.id());
        if (snapshot != null) {
            this.states.restore(player, snapshot);
        } else {
            this.states.reset(player);
        }
    }

}
