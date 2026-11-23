package codes.reason.fibre.bukkit;

import codes.reason.fibre.game.GameManager;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NullMarked;

// a player who quits leaves their game first, so the server saves their state from outside any game
@NullMarked
final class QuitListener implements Listener {

    private final GameManager games;

    QuitListener(GameManager games) {
        this.games = games;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.games.findByPlayer(id).ifPresent(game -> game.removePlayer(id));
    }

}
