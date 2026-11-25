package codes.reason.fibre.bukkit;

import codes.reason.fibre.Registration;
import codes.reason.fibre.event.EventOrder;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.world.WorldPosition;
import codes.reason.fibre.platform.PlatformEvents;
import java.util.Optional;
import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class BukkitEvents implements PlatformEvents {

    private final Plugin plugin;
    private final EventLookup lookup;
    private final BukkitAdapters adapters;

    BukkitEvents(Plugin plugin, EventLookup lookup, BukkitAdapters adapters) {
        this.plugin = plugin;
        this.lookup = lookup;
        this.adapters = adapters;
    }

    @Override
    public Class<?> baseType() {
        return Event.class;
    }

    @Override
    public <E> Registration subscribe(Class<E> eventType, EventOrder order, Consumer<? super E> handler) {
        if (!Event.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException("%s is not a Bukkit event".formatted(eventType.getName()));
        }
        BukkitRegistration registration = new BukkitRegistration();
        this.plugin.getServer().getPluginManager().registerEvent(
                eventType.asSubclass(Event.class),
                registration,
                priority(order),
                (_, event) -> {
                    // Bukkit also calls executors for subclasses of the event, which are still wanted
                    if (eventType.isInstance(event)) {
                        handler.accept(eventType.cast(event));
                    }
                },
                this.plugin);
        return registration;
    }

    @Override
    public Optional<PlayerAdapter<?>> player(Object event) {
        return this.lookup.player((Event) event).map(this.adapters::player);
    }

    @Override
    public Optional<WorldPosition> position(Object event) {
        return this.lookup.location((Event) event)
                .filter(location -> location.getWorld() != null)
                .map(this.adapters::position);
    }

    private static EventPriority priority(EventOrder order) {
        // MONITOR is left out, since game listeners are allowed to change the event
        return switch (order) {
            case FIRST -> EventPriority.LOWEST;
            case EARLY -> EventPriority.LOW;
            case NORMAL -> EventPriority.NORMAL;
            case LATE -> EventPriority.HIGH;
            case LAST -> EventPriority.HIGHEST;
        };
    }

    private static final class BukkitRegistration implements Listener, Registration {

        private boolean registered = true;

        @Override
        public void unregister() {
            if (this.registered) {
                this.registered = false;
                HandlerList.unregisterAll(this);
            }
        }

        @Override
        public boolean isRegistered() {
            return this.registered;
        }

    }

}
