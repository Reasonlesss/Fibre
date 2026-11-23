package codes.reason.fibre.platform;

import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.world.WorldAdapter;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FibrePlatform {

    PlatformAdapters adapters();

    PlatformEvents events();

    Scheduler scheduler();

    PlayerStates playerStates();

    Logger logger();

    boolean isServerThread();

    Optional<PlayerAdapter<?>> player(UUID id);

    Optional<WorldAdapter<?>> world(String name);

}
