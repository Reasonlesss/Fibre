package cloud.emilys.fibre.api.container;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PerPlayer<T> extends Iterable<T> {

    T get(Player player);
}
