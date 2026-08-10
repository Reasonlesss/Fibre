package codes.reason.fibre.features.event;

import codes.reason.fibre.api.Fibre;
import codes.reason.fibre.api.event.GameEvent;
import codes.reason.fibre.api.game.Game;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EventFeature {

    private EventFeature() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        fibre.getStartupRegistry().registerFactScanner(new EventFactScanner());
        fibre.getStartupRegistry().registerScopeInitializer(new EventInitializer());
        fibre.getStartupRegistry().registerScopeContributor(new EventContributor());
        fibre.getStartupRegistry()
                .registerTypeFinder(GameEvent.class, Game.class, event -> Optional.of(event.getGame()));
    }
}
