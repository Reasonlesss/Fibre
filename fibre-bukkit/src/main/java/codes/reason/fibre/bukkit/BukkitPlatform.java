package codes.reason.fibre.bukkit;

import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.world.WorldAdapter;
import codes.reason.fibre.platform.FibrePlatform;
import codes.reason.fibre.platform.PlatformAdapters;
import codes.reason.fibre.platform.PlatformEvents;
import codes.reason.fibre.platform.PlayerStates;
import codes.reason.fibre.platform.Scheduler;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntFunction;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BukkitPlatform implements FibrePlatform {

    private final JavaPlugin plugin;
    private final BukkitAdapters adapters;
    private final BukkitEvents events;
    private final BukkitTasks scheduler;
    private final PlayerStates playerStates;
    private final Respawns respawns;
    private final boolean audiences;

    public BukkitPlatform(JavaPlugin plugin, EventLookup lookup, PlayerStates playerStates, Respawns respawns, ToIntFunction<World> minHeight, boolean audiences) {
        this.plugin = plugin;
        this.respawns = respawns;
        this.audiences = audiences;
        this.playerStates = playerStates;
        this.adapters = new BukkitAdapters(minHeight);
        this.events = new BukkitEvents(plugin, lookup, this.adapters);
        this.scheduler = new BukkitTasks(plugin);
    }

    // registered by the plugin, so state set on dead players survives their respawn
    public Respawns respawns() {
        return this.respawns;
    }

    // whether players are Adventure audiences, so AudiencePlugin can be installed
    public boolean audiences() {
        return this.audiences;
    }

    @Override
    public PlatformAdapters adapters() {
        return this.adapters;
    }

    @Override
    public PlayerStates playerStates() {
        return this.playerStates;
    }

    @Override
    public PlatformEvents events() {
        return this.events;
    }

    @Override
    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public Logger logger() {
        return this.plugin.getLogger();
    }

    @Override
    public boolean isServerThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public Optional<PlayerAdapter<?>> player(UUID id) {
        return Optional.ofNullable(Bukkit.getPlayer(id)).map(this.adapters::player);
    }

    @Override
    public Optional<WorldAdapter<?>> world(String name) {
        return Optional.ofNullable(Bukkit.getWorld(name)).map(this.adapters::world);
    }

}
