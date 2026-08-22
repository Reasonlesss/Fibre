package cloud.emilys.fibre.features.player;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.game.PlayerJoinToken;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerJoinListener implements Listener {

    private final Fibre fibre;
    private final JavaPlugin plugin;

    public PlayerJoinListener(Fibre fibre, JavaPlugin plugin) {
        this.fibre = Objects.requireNonNull(fibre, "fibre");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerId = event.getUniqueId();
        try {
            this.fibre.getGameManager().findPlayerJoinToken(playerId).ifPresent(PlayerJoinToken::await);
        } catch (CompletionException | CancellationException failure) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Your game data could not be prepared. Please try again.");
            this.plugin.getLogger().log(Level.SEVERE, "Could not prepare game data for player " + playerId, failure);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.fibre
                .getGameManager()
                .findPlayerJoinToken(event.getPlayer().getUniqueId())
                .ifPresent(token -> token.join(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.fibre.getGameManager().findGame(event.getPlayer()).ifPresent(game -> game.removePlayer(event.getPlayer()));
    }
}
