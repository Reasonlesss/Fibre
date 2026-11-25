package codes.reason.fibre;

import codes.reason.fibre.game.FibreEvents;
import codes.reason.fibre.game.GameManager;
import codes.reason.fibre.plugin.PluginManager;
import codes.reason.fibre.platform.FibrePlatform;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Fibre {

    static Fibre get() {
        return FibreHolder.get();
    }

    GameManager games();

    PluginManager plugins();

    // every game's events, with the game each came from
    default FibreEvents events() {
        return this.games().events();
    }

    FibrePlatform platform();

}
