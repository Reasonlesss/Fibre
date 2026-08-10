package cloud.emilys.fibre.api.game;

import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface Players {

    List<Player> getPlayers();
}
