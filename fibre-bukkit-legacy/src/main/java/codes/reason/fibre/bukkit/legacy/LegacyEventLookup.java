package codes.reason.fibre.bukkit.legacy;

import static codes.reason.fibre.bukkit.EventLookup.asPlayer;
import static codes.reason.fibre.bukkit.EventLookup.inventoryLocation;
import static codes.reason.fibre.bukkit.EventLookup.inventoryPlayer;
import static codes.reason.fibre.bukkit.EventLookup.senderLocation;

import codes.reason.fibre.bukkit.EventFields;
import codes.reason.fibre.bukkit.EventLookup;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.painting.PaintingEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class LegacyEventLookup implements EventLookup {

    private final EventFields fields = new EventFields(List.of("org.bukkit.", "org.spigotmc."));

    @Override
    @SuppressWarnings("deprecation")
    public Optional<Player> player(Event event) {
        Player player = switch (event) {
            case PlayerEvent e -> e.getPlayer();
            case BlockBreakEvent e -> e.getPlayer();
            case BlockDamageEvent e -> e.getPlayer();
            case BlockIgniteEvent e -> e.getPlayer();
            case BlockPlaceEvent e -> e.getPlayer();
            case SignChangeEvent e -> e.getPlayer();
            case PlayerLeashEntityEvent e -> e.getPlayer();
            case HangingPlaceEvent e -> e.getPlayer();
            case FurnaceExtractEvent e -> e.getPlayer();
            case PaintingPlaceEvent e -> e.getPlayer();
            case PlayerUnleashEntityEvent e -> e.getPlayer();
            case StructureGrowEvent e -> e.getPlayer();
            case InventoryEvent e -> inventoryPlayer(e.getInventory());
            case InventoryMoveItemEvent e -> inventoryPlayer(e.getInitiator());
            case ServerCommandEvent e -> asPlayer(e.getSender());
            case EntityEvent e -> asPlayer(e.getEntity());
            case VehicleEvent e -> asPlayer(e.getVehicle());
            case BlockEvent _, HangingEvent _, PaintingEvent _, WorldEvent _, WeatherEvent _, ServerEvent _ -> null;
            default -> this.fields.get(event, Player.class).orElse(null);
        };
        return Optional.ofNullable(player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Optional<Location> location(Event event) {
        Location location = switch (event) {
            case BlockEvent e -> e.getBlock().getLocation();
            case PlayerEvent e -> e.getPlayer().getLocation();
            case EntityEvent e -> e.getEntity().getLocation();
            case HangingEvent e -> e.getEntity().getLocation();
            case PaintingEvent e -> e.getPainting().getLocation();
            case VehicleEvent e -> e.getVehicle().getLocation();
            case PlayerLeashEntityEvent e -> e.getEntity().getLocation();
            case InventoryEvent e -> inventoryLocation(e.getInventory());
            case InventoryMoveItemEvent e -> inventoryLocation(e.getSource());
            case InventoryPickupItemEvent e -> e.getItem().getLocation();
            case LightningStrikeEvent e -> e.getLightning().getLocation();
            case StructureGrowEvent e -> e.getLocation();
            case ServerCommandEvent e -> senderLocation(e.getSender());
            case WorldEvent _, WeatherEvent _, ServerEvent _ -> null;
            default -> this.fields.location(event).orElse(null);
        };
        return Optional.ofNullable(location);
    }

}
