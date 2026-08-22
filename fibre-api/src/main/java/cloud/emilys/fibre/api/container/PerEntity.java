package cloud.emilys.fibre.api.container;

import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PerEntity<T> extends Iterable<T> {

    T get(Entity entity);

    void track(Entity entity);

    void untrack(Entity entity);
}
