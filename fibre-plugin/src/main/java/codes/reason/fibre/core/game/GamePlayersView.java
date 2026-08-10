package codes.reason.fibre.core.game;

import codes.reason.fibre.api.game.Game;
import codes.reason.fibre.api.game.Players;
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
