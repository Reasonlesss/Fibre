package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.PrimaryThreadUtil;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.GameBuilder;
import cloud.emilys.fibre.api.game.GameManager;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GameManagerImpl implements GameManager {

    private final Fibre api;
    private final Set<GameImpl> instances = new LinkedHashSet<>();
    private final Map<UUID, GameImpl> gamesByPlayer = new LinkedHashMap<>();

    public GameManagerImpl(Fibre api) {
        this.api = Objects.requireNonNull(api, "api");
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
        this.gamesByPlayer.entrySet().removeIf(entry -> entry.getValue() == instance);
    }

    @Override
    public List<Game> getGames() {
        PrimaryThreadUtil.assertPrimary();
        return List.copyOf(this.instances);
    }

    @Override
    public Optional<Game> findGame(Object object) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(object, "object");

        if (object instanceof Player player) {
            GameImpl game = this.gamesByPlayer.get(player.getUniqueId());
            return game == null ? Optional.empty() : Optional.of(game);
        }

        if (object instanceof GameImpl game) {
            return this.instances.contains(game) ? Optional.of(game) : Optional.empty();
        }

        return this.api.getTypeResolver().find(object, Game.class).filter(this::isRegistered);
    }

    void addPlayer(GameImpl game, Player player) {
        PrimaryThreadUtil.assertPrimary();
        this.assertRegistered(game);
        Objects.requireNonNull(player, "player");

        if (this.gamesByPlayer.putIfAbsent(player.getUniqueId(), game) != null) {
            throw new IllegalStateException("Player is already in a game");
        }
    }

    void removePlayer(GameImpl game, Player player) {
        PrimaryThreadUtil.assertPrimary();
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(player, "player");

        if (!this.gamesByPlayer.remove(player.getUniqueId(), game)) {
            throw new IllegalStateException("Player is not in this game");
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
}
