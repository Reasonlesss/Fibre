package cloud.emilys.fibre.features.adventure;

import cloud.emilys.fibre.api.game.Players;
import java.util.Iterator;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayersAudience implements ForwardingAudience {

    private final Players players;
    private final PlayerAudienceMapping mapping;

    public PlayersAudience(Players players, PlayerAudienceMapping mapping) {
        this.players = players;
        this.mapping = mapping;
    }

    @Override
    public Iterable<? extends Audience> audiences() {
        return () -> {
            Iterator<Player> iterator = this.players.getPlayers().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Audience next() {
                    return mapping.apply(iterator.next());
                }
            };
        };
    }
}
