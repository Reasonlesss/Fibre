package codes.reason.fibre.player;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.entity.EntityAdapter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlayerAdapter<P> extends EntityAdapter<P> {

    static PlayerAdapter<?> of(Object player) {
        return Fibre.get().platform().adapters().player(player);
    }

    String username();

}
