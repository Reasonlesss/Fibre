package codes.reason.fibre.game;

import codes.reason.fibre.event.GamePlayerAddEvent;
import codes.reason.fibre.event.GamePlayerRemoveEvent;
import codes.reason.fibre.player.PlayerAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GamePlayers {

    private final GameEvents events;
    private final Map<UUID, PlayerAdapter<?>> players = new LinkedHashMap<>();
    private boolean closed;

    GamePlayers(GameEvents events) {
        this.events = events;
    }

    boolean add(PlayerAdapter<?> player) {
        Objects.requireNonNull(player, "player must not be null");
        if (this.closed) {
            throw new IllegalStateException("Can't add a player to a closed game");
        }
        if (this.players.putIfAbsent(player.id(), player) != null) {
            return false;
        }
        this.events.call(new GamePlayerAddEvent(player));
        return true;
    }

    boolean remove(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        PlayerAdapter<?> player = this.players.remove(id);
        if (player == null) {
            return false;
        }
        this.events.call(new GamePlayerRemoveEvent(player));
        return true;
    }

    boolean remove(PlayerAdapter<?> player) {
        Objects.requireNonNull(player, "player must not be null");
        return this.remove(player.id());
    }

    public boolean contains(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return this.players.containsKey(id);
    }

    public boolean contains(PlayerAdapter<?> player) {
        Objects.requireNonNull(player, "player must not be null");
        return this.contains(player.id());
    }

    public Optional<PlayerAdapter<?>> get(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(this.players.get(id));
    }

    public List<PlayerAdapter<?>> all() {
        return List.copyOf(this.players.values());
    }

    public int size() {
        return this.players.size();
    }

    void close() {
        this.closed = true;
        for (UUID id : List.copyOf(this.players.keySet())) {
            this.remove(id);
        }
    }

}
