package cloud.emilys.fibre.api.scope;

import cloud.emilys.fibre.api.data.RuntimeDataOwner;
import cloud.emilys.fibre.api.game.Game;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Scope extends AutoCloseable, RuntimeDataOwner, ScopeResolver {

    String getName();

    void setName(String name);

    Game getGame();

    default JavaPlugin getPlugin() {
        return this.getGame().getPlugin();
    }

    default @Nullable Scope getParent() {
        List<Scope> parents = this.getParents();
        return parents.isEmpty() ? null : parents.getFirst();
    }

    List<Scope> getParents();

    List<Scope> getChildren();

    void bindParent(Scope parent);

    <T> void addFilter(Class<T> type, Filter<? super T> filter);

    boolean accepts(Object value);

    List<ScopedObject> getLocalObjects();

    @Override
    void close();
}
