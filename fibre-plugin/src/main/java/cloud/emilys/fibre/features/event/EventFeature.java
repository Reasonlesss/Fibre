package cloud.emilys.fibre.features.event;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.event.GameEvent;
import cloud.emilys.fibre.api.game.Game;
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
