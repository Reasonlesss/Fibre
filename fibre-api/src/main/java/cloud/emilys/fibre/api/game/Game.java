package cloud.emilys.fibre.api.game;

import cloud.emilys.fibre.api.data.RuntimeDataOwner;
import cloud.emilys.fibre.api.scope.Scope;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Game extends RuntimeDataOwner {

    JavaPlugin getPlugin();

    Scope getRootScope();

    PlayerJoinToken createPlayerJoinToken(UUID playerId);

    boolean removePlayer(Player player);

    /** Returns the players whose join phase has completed and who are currently online. */
    List<Player> getPlayers();

    void complete(Object result);

    void handleException(Throwable exception);
}
