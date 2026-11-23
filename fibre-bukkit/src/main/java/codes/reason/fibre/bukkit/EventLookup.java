package codes.reason.fibre.bukkit;

import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// which player caused an event, or where it happened; each server version knows its own events
@NullMarked
public interface EventLookup {

    Optional<Player> player(Event event);

    Optional<Location> location(Event event);

    static @Nullable Player inventoryPlayer(Inventory inventory) {
        return inventory.getViewers().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .findFirst()
                .orElseGet(() -> asPlayer(inventory.getHolder()));
    }

    static @Nullable Location inventoryLocation(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        return switch (holder) {
            case Entity entity -> entity.getLocation();
            case BlockState state -> state.getLocation();
            case DoubleChest chest -> chest.getLocation();
            case null, default -> inventory.getViewers().stream().findFirst().map(Entity::getLocation).orElse(null);
        };
    }

    static @Nullable Location senderLocation(CommandSender sender) {
        return switch (sender) {
            case Entity entity -> entity.getLocation();
            case BlockCommandSender block -> block.getBlock().getLocation();
            default -> null;
        };
    }

    static @Nullable Player asPlayer(@Nullable Object object) {
        return object instanceof Player player ? player : null;
    }

}
