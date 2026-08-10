package codes.reason.fibre.api.game;

import codes.reason.fibre.api.data.RuntimeDataOwner;
import codes.reason.fibre.api.scope.Scope;
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
