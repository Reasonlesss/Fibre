package codes.reason.fibre.platform;

import codes.reason.fibre.Registration;
import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.world.WorldPosition;
import java.util.Optional;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlatformEvents {

    Class<?> baseType();

    <E> Registration subscribe(Class<E> eventType, EventOrder order, Consumer<? super E> handler);

    Optional<PlayerAdapter<?>> player(Object event);

    Optional<WorldPosition> position(Object event);

}
