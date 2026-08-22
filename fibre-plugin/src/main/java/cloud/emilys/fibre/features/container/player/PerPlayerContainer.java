package cloud.emilys.fibre.features.container.player;

import cloud.emilys.fibre.api.container.PerPlayer;
import cloud.emilys.fibre.api.event.GamePlayerAddEvent;
import cloud.emilys.fibre.api.event.GamePlayerRemoveEvent;
import cloud.emilys.fibre.api.game.Players;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import cloud.emilys.fibre.core.util.ResourceCleanup;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class PerPlayerContainer<T> implements PerPlayer<T>, Listener, AutoCloseable {

    private final ObjectKey valueKey;
    private final Players players;
    private final Map<UUID, Scope> scopes = new LinkedHashMap<>();
    private @Nullable Scope parent;
    private boolean active;

    public PerPlayerContainer(ObjectKey valueKey, Players players) {
        this.valueKey = valueKey;
        this.players = players;
    }

    public void initialize(Scope parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Per-player container is already initialized");
        }
        this.parent = parent;
    }

    public void activate() {
        Scope parent = this.parent;
        if (parent == null) {
            throw new IllegalStateException("Per-player container is not initialized");
        }
        if (this.active) {
            throw new IllegalStateException("Per-player container is already active");
        }
        this.active = true;
        for (Player player : this.players.getPlayers()) {
            this.add(player);
        }
        Bukkit.getPluginManager().registerEvents(this, parent.getPlugin());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(Player player) {
        if (!this.active) {
            throw new IllegalStateException("Per-player container is not active during scope configuration");
        }
        Scope scope = this.scopes.get(player.getUniqueId());
        if (scope == null) {
            throw new IllegalArgumentException("Player is not tracked by this container");
        }
        return (T) scope.require(this.valueKey).getObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        if (!this.active) {
            throw new IllegalStateException("Per-player container is not active during scope configuration");
        }
        Iterator<Scope> scopes = this.scopes.values().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return scopes.hasNext();
            }

            @Override
            public T next() {
                //noinspection resource
                return (T)
                        scopes.next().require(PerPlayerContainer.this.valueKey).getObject();
            }
        };
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAdd(GamePlayerAddEvent event) {
        Scope parent = this.parent;
        if (parent != null && event.getGame() == parent.getGame()) {
            try {
                this.add(event.getPlayer());
            } catch (RuntimeException exception) {
                parent.getGame().handleException(exception);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRemove(GamePlayerRemoveEvent event) {
        Scope parent = this.parent;
        if (parent != null && event.getGame() == parent.getGame()) {
            this.remove(event.getPlayer());
        }
    }

    private void add(Player player) {
        UUID playerId = player.getUniqueId();
        if (this.scopes.containsKey(playerId)) {
            return;
        }
        Scope parent = this.parent;
        if (parent == null) {
            throw new IllegalStateException("Per-player container is not initialized");
        }
        Scope scope = ScopeFactory.synchronous()
                .initialize(ScopeBlueprint.builder()
                        .setParent(parent)
                        .setGame(parent.getGame())
                        .setInitialKey(this.valueKey)
                        .setInputObject(Player.class, player)
                        .build());
        scope.setName("PerPlayer<" + this.valueKey.getObjectClass().getSimpleName() + "> " + player.getName());
        scope.addFilter(Player.class, candidate -> candidate.getUniqueId().equals(playerId));
        this.scopes.put(playerId, scope);
    }

    private void remove(Player player) {
        UUID playerId = player.getUniqueId();
        Scope scope = this.scopes.get(playerId);
        if (scope == null) {
            return;
        }
        this.scopes.remove(playerId).close();
    }

    @Override
    public void close() {
        HandlerList.unregisterAll(this);
        ResourceCleanup.closeAllQuietly(List.copyOf(this.scopes.values()));
        this.scopes.clear();
        this.active = false;
        this.parent = null;
    }
}
