package cloud.emilys.fibre.features.event;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.event.EventInvocation;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.lifecycle.ScopeInitializer;
import java.util.Optional;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EventInitializer implements ScopeInitializer {

    @Override
    public void initialize(Scope scope) {
        scope.addFilter(Event.class, event -> acceptsEvent(scope, event));
        scope.addFilter(EventInvocation.class, this::acceptsInvocation);
    }

    private boolean acceptsEvent(Scope scope, Event event) {
        Fibre fibre = Fibre.get();
        EventSources sources = findSources(fibre, event);

        if (!sources.entity().map(scope::accepts).orElse(true)
                || !sources.world().map(scope::accepts).orElse(true)) {
            return false;
        }

        return findGame(fibre, event, sources)
                .map(instance -> instance.equals(scope.getGame()))
                .orElse(true);
    }

    private boolean acceptsInvocation(EventInvocation invocation) {
        if (!invocation.ignoresGlobal()) {
            return true;
        }

        Fibre fibre = Fibre.get();
        Event event = invocation.getEvent();
        return findGame(fibre, event, findSources(fibre, event)).isPresent();
    }

    private EventSources findSources(Fibre fibre, Event event) {
        return new EventSources(
                fibre.getTypeResolver().find(event, Entity.class),
                fibre.getTypeResolver().find(event, World.class));
    }

    private Optional<Game> findGame(Fibre fibre, Event event, EventSources sources) {
        return fibre.getTypeResolver()
                .find(event, Game.class)
                .or(() -> sources.entity().flatMap(fibre.getGameManager()::findGame))
                .or(() -> sources.world().flatMap(fibre.getGameManager()::findGame));
    }

    @NullMarked
    private record EventSources(Optional<Entity> entity, Optional<World> world) {}
}
