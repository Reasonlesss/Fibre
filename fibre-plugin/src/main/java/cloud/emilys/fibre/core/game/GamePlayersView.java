package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.Players;
import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GamePlayersView implements Players {

    private final Game game;

    public GamePlayersView(Game game) {
        this.game = game;
    }

    @Override
    public List<Player> getPlayers() {
        return this.game.getPlayers();
    }
}
