package codes.reason.fibre.builtin.audience;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.game.GamePlayers;
import codes.reason.fibre.plugin.ScopePlugin;
import codes.reason.fibre.scope.Handle;
import codes.reason.fibre.scope.ScopeInject;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AudiencePlugin implements ScopePlugin {

    @Override
    public void inject(Fibre fibre, ScopeInject inject) {
        // an audience someone else bound is kept; ours is rebound per scope, since each scope filters differently
        if (inject.get(Audience.class).filter(audience -> !(audience instanceof PlayersAudience)).isPresent()) {
            return;
        }
        inject.get(GamePlayers.class).ifPresent(players ->
                inject.bind(Audience.class, new PlayersAudience(players, inject.require(Handle.SCOPE))));
    }

}
