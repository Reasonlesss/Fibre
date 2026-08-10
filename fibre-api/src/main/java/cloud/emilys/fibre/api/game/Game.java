package cloud.emilys.fibre.api.game;

import cloud.emilys.fibre.api.data.RuntimeDataOwner;
import cloud.emilys.fibre.api.scope.Scope;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Game extends RuntimeDataOwner {

    JavaPlugin getPlugin();

    Scope getRootScope();

    boolean tryAddPlayer(Player player);

    boolean removePlayer(Player player);

    List<Player> getPlayers();

    void complete(Object result);

    void handleException(Throwable exception);
}
