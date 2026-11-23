package codes.reason.fibre.builtin.audience;

import codes.reason.fibre.builtin.filter.FilterPlugin;
import codes.reason.fibre.game.GamePlayers;
import codes.reason.fibre.scope.Handle;
import codes.reason.fibre.scope.Scope;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jspecify.annotations.NullMarked;

// the game's players that the scope's filters allow
@NullMarked
final class PlayersAudience implements ForwardingAudience {

    private final GamePlayers players;
    private final Handle<Scope> scope;

    PlayersAudience(GamePlayers players, Handle<Scope> scope) {
        this.players = players;
        this.scope = scope;
    }

    @Override
    public Iterable<? extends Audience> audiences() {
        Scope scope = this.scope.require();
        return this.players.all().stream()
                .filter(player -> FilterPlugin.allows(scope, player, player.get()))
                .flatMap(player -> player.as(Audience.class).stream())
                .toList();
    }

}
