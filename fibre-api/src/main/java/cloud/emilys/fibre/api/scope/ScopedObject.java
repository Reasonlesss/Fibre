package cloud.emilys.fibre.api.scope;

import cloud.emilys.fibre.api.data.RuntimeDataOwner;
import cloud.emilys.fibre.api.fact.Facts;
import cloud.emilys.fibre.api.game.Game;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopedObject extends RuntimeDataOwner, AutoCloseable {

    void track(AutoCloseable closeable);

    Object getObject();

    Scope getScope();

    default Game getGame() {
        return this.getScope().getGame();
    }

    default JavaPlugin getPlugin() {
        return this.getGame().getPlugin();
    }

    Facts getFacts();
}
