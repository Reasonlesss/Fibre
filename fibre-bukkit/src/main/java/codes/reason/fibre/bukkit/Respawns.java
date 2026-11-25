package codes.reason.fibre.bukkit;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.game.GameHolder;
import codes.reason.fibre.player.PlayerAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// Respawning gives a player a fresh body and default state, so anything set on them while dead would be lost. State
// changes made then wait here until they've respawned, and they respawn where they were last placed. Players who die
// in a game respawn at its spawn. Both are set first, so a game's own respawn listeners can still change them.
@NullMarked
public final class Respawns implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Pending> pending = new HashMap<>();

    public Respawns(Plugin plugin) {
        this.plugin = plugin;
    }

    // run in order once the player has respawned
    public void afterRespawn(Player player, Consumer<Player> action) {
        this.pending(player).actions.add(action);
    }

    // where the player respawns; the latest one wins
    public void respawnAt(Player player, Location location) {
        this.pending(player).location = location;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Pending pending = this.pending.remove(player.getUniqueId());
        if (pending != null) {
            Location location = pending.location;
            if (location != null && isLoaded(location)) {
                event.setRespawnLocation(location);
            }
            // a tick later, once the server has finished setting up the respawned player
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (player.isOnline()) {
                    pending.actions.forEach(action -> action.accept(player));
                }
            });
            return;
        }
        gameSpawn(player).ifPresent(event::setRespawnLocation);
    }

    // a player who left while dead may come back alive, and then there's no respawn to wait for
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isDead()) {
            return;
        }
        Pending pending = this.pending.remove(player.getUniqueId());
        if (pending == null) {
            return;
        }
        if (pending.location != null && isLoaded(pending.location)) {
            player.teleport(pending.location);
        }
        pending.actions.forEach(action -> action.accept(player));
    }

    private Pending pending(Player player) {
        return this.pending.computeIfAbsent(player.getUniqueId(), _ -> new Pending());
    }

    private static Optional<Location> gameSpawn(Player player) {
        GameHolder game = Fibre.get().games().findByPlayer(player.getUniqueId()).orElse(null);
        if (game == null) {
            return Optional.empty();
        }
        return game.spawn(PlayerAdapter.of(player)).as(Location.class);
    }

    private static boolean isLoaded(Location location) {
        return location.getWorld() != null && Bukkit.getWorld(location.getWorld().getUID()) != null;
    }

    private static final class Pending {

        private final List<Consumer<Player>> actions = new ArrayList<>();
        private @Nullable Location location;
    }

}
