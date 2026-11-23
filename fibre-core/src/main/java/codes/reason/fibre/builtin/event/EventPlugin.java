package codes.reason.fibre.builtin.event;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.builtin.filter.FilterPlugin;
import codes.reason.fibre.game.GameEvents;
import codes.reason.fibre.game.GameExecutor;
import codes.reason.fibre.game.GameIdentity;
import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.event.GameEvent;
import codes.reason.fibre.event.GamePlayerEvent;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.platform.FibrePlatform;
import codes.reason.fibre.plugin.ScopePlugin;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.ScopeSetup;
import codes.reason.fibre.scope.member.BoundMethod;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EventPlugin implements ScopePlugin {

    private final EventOwners owners = new EventOwners();

    @Override
    public void setup(Fibre fibre, ScopeSetup setup) {
        GameExecutor executor = setup.require(GameExecutor.class);
        GameEvents gameEvents = setup.require(GameEvents.class);
        GameIdentity identity = setup.require(GameIdentity.class);
        FibrePlatform platform = fibre.platform();
        Scope scope = setup.scope();

        for (BoundMethod method : setup.localMethods(Listen.class)) {
            Class<?> type = method.method().getParameterTypes()[0];
            EventOrder order = method.method().getAnnotation(Listen.class).order();
            if (GameEvent.class.isAssignableFrom(type)) {
                setup.closeable(gameEvents.register(type.asSubclass(GameEvent.class), order,
                        event -> {
                            PlayerAdapter<?> player = event instanceof GamePlayerEvent playerEvent ? playerEvent.player() : null;
                            if (FilterPlugin.allows(scope, event, player, player != null ? player.get() : null)) {
                                executor.execute(() -> method.invoke(event));
                            }
                        }));
                continue;
            }

            method.signature()
                    .parameterSubtype(0, platform.events().baseType())
                    .check("@Listen");
            setup.closeable(platform.events().subscribe(type, order, (Object event) -> {
                // game state is single threaded, so async events are ignored
                if (!platform.isServerThread()) {
                    return;
                }
                EventOwners.Origin origin = this.owners.origin(fibre, event);
                PlayerAdapter<?> player = origin.player();
                if (origin.game() != null && origin.game().identity().equals(identity)
                        && FilterPlugin.allows(scope, event, player, player != null ? player.get() : null)) {
                    executor.execute(() -> method.invoke(event));
                }
            }));
        }
    }

}
