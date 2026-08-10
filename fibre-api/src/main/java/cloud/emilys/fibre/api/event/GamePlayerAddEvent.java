package cloud.emilys.fibre.api.event;

import cloud.emilys.fibre.api.game.Game;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GamePlayerAddEvent extends Event implements GameEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Game game;
    private final Player player;

    public GamePlayerAddEvent(Game game, Player player) {
        this.game = Objects.requireNonNull(game, "game");
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
