package codes.reason.fibre.platform;

import codes.reason.fibre.entity.EntityAdapter;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.world.WorldAdapter;
import codes.reason.fibre.world.WorldPosition;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlatformAdapters {

    WorldAdapter<?> world(Object world);

    EntityAdapter<?> entity(Object entity);

    PlayerAdapter<?> player(Object player);

    WorldPosition position(Object location);

}
