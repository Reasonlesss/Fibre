package cloud.emilys.fibre.api.container;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PerPlayer<T> {

    T get(Player player);
}
