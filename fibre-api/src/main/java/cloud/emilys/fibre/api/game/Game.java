package cloud.emilys.fibre.api.game;

import cloud.emilys.fibre.api.data.RuntimeDataOwner;
import cloud.emilys.fibre.api.scope.Scope;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Game extends RuntimeDataOwner {

    JavaPlugin getPlugin();

    Scope getRootScope();

    /** Reserves a place for the player and starts preloading their data. */
    default PlayerJoinToken addPlayer(UUID playerId) {
        return this.addPlayer(playerId, _ -> {});
    }

    /** Reserves a place for the player and starts preloading their data, including the work added by {@code preload}. */
    PlayerJoinToken addPlayer(UUID playerId, Consumer<? super PreloadSink> preload);

    boolean removePlayer(Player player);

    /** Returns the players whose join phase has completed and who are currently online. */
    List<Player> getPlayers();

    void complete(Object result);

    void handleException(Throwable exception);
}
