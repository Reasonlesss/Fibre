package codes.reason.fibre.bukkit.paper;

import static codes.reason.fibre.bukkit.EventLookup.asPlayer;
import static codes.reason.fibre.bukkit.EventLookup.inventoryLocation;
import static codes.reason.fibre.bukkit.EventLookup.inventoryPlayer;
import static codes.reason.fibre.bukkit.EventLookup.senderLocation;

import codes.reason.fibre.bukkit.EventFields;
import codes.reason.fibre.bukkit.EventLookup;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.block.BellRevealRaiderEvent;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import io.papermc.paper.event.block.BlockLockCheckEvent;
import io.papermc.paper.event.block.VaultChangeStateEvent;
import io.papermc.paper.event.entity.EntityDyeEvent;
import io.papermc.paper.event.inventory.ItemCraftedEvent;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.command.UnknownCommandEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
final class PaperEventLookup implements EventLookup {

    private final EventFields fields = new EventFields(
            List.of("org.bukkit.", "org.spigotmc.", "io.papermc.paper.", "com.destroystokyo.paper."));

    @Override
    @SuppressWarnings({"deprecation", "removal"})
    public Optional<Player> player(Event event) {
        Player player = switch (event) {
            case PlayerEvent e -> e.getPlayer();
            case BlockBreakEvent e -> e.getPlayer();
            case BlockCanBuildEvent e -> e.getPlayer();
            case BlockDamageAbortEvent e -> e.getPlayer();
            case BlockDamageEvent e -> e.getPlayer();
            case BlockDispenseArmorEvent e -> asPlayer(e.getTargetEntity());
            case BlockDispenseLootEvent e -> e.getPlayer();
            case BlockDropItemEvent e -> e.getPlayer();
            case BlockFertilizeEvent e -> e.getPlayer();
            case BlockIgniteEvent e -> e.getPlayer();
            case BlockPlaceEvent e -> e.getPlayer();
            case BlockReceiveGameEvent e -> asPlayer(e.getEntity());
            case BlockShearEntityEvent e -> asPlayer(e.getEntity());
            case CauldronLevelChangeEvent e -> asPlayer(e.getEntity());
            case EntityBlockFormEvent e -> asPlayer(e.getEntity());
            case SignChangeEvent e -> e.getPlayer();
            case TNTPrimeEvent e -> asPlayer(e.getPrimingEntity());
            case BellRingEvent e -> asPlayer(e.getEntity());
            case EntityPlaceEvent e -> e.getPlayer();
            case PlayerLeashEntityEvent e -> e.getPlayer();
            case HangingPlaceEvent e -> e.getPlayer();
            case FurnaceExtractEvent e -> e.getPlayer();
            case PlayerUnleashEntityEvent e -> e.getPlayer();
            case RaidTriggerEvent e -> e.getPlayer();
            case StructureGrowEvent e -> e.getPlayer();
            case BeaconEffectEvent e -> e.getPlayer();
            case EndermanAttackPlayerEvent e -> e.getPlayer();
            case BlockLockCheckEvent e -> e.getPlayer();
            case VaultChangeStateEvent e -> e.getPlayer();
            case BellRevealRaiderEvent e -> asPlayer(e.getEntity());
            case BlockBreakProgressUpdateEvent e -> asPlayer(e.getEntity());
            case com.destroystokyo.paper.event.block.TNTPrimeEvent e -> asPlayer(e.getPrimerEntity());
            case EntityDyeEvent e -> e.getPlayer();
            case ItemCraftedEvent e -> e.getPlayer();
            case PlayerChunkLoadEvent e -> e.getPlayer();
            case PlayerChunkUnloadEvent e -> e.getPlayer();
            case PlayerCustomClickEvent e -> clicker(e);
            case InventoryEvent e -> inventoryPlayer(e.getInventory());
            case InventoryMoveItemEvent e -> inventoryPlayer(e.getInitiator());
            case UnknownCommandEvent e -> asPlayer(e.getSender());
            case ServerCommandEvent e -> asPlayer(e.getSender());
            case TabCompleteEvent e -> asPlayer(e.getSender());
            case AsyncTabCompleteEvent e -> asPlayer(e.getSender());
            case EntityEvent e -> asPlayer(e.getEntity());
            case VehicleEvent e -> asPlayer(e.getVehicle());
            case BlockEvent _, HangingEvent _, WorldEvent _, WeatherEvent _, ServerEvent _,
                 ServerTickStartEvent _, ServerTickEndEvent _ -> null;
            default -> this.fields.get(event, Player.class).orElse(null);
        };
        return Optional.ofNullable(player);
    }

    @Override
    @SuppressWarnings({"deprecation", "removal"})
    public Optional<Location> location(Event event) {
        Location location = switch (event) {
            case BlockEvent e -> e.getBlock().getLocation();
            case PlayerEvent e -> e.getPlayer().getLocation();
            case EntityEvent e -> e.getEntity().getLocation();
            case HangingEvent e -> e.getEntity().getLocation();
            case VehicleEvent e -> e.getVehicle().getLocation();
            case PlayerLeashEntityEvent e -> e.getEntity().getLocation();
            case InventoryEvent e -> inventoryLocation(e.getInventory());
            case InventoryMoveItemEvent e -> inventoryLocation(e.getSource());
            case InventoryPickupItemEvent e -> e.getItem().getLocation();
            case ItemCraftedEvent e -> e.getPlayer().getLocation();
            case ThrownEggHatchEvent e -> e.getEgg().getLocation();
            case PreCreatureSpawnEvent e -> e.getSpawnLocation();
            case LightningStrikeEvent e -> e.getLightning().getLocation();
            case StructureGrowEvent e -> e.getLocation();
            case PlayerCustomClickEvent e -> {
                Player clicker = clicker(e);
                yield clicker != null ? clicker.getLocation() : null;
            }
            case UnknownCommandEvent e -> e.getCommandSource().getLocation();
            case ServerCommandEvent e -> senderLocation(e.getSender());
            case TabCompleteEvent e -> e.getLocation() != null ? e.getLocation() : senderLocation(e.getSender());
            case AsyncTabCompleteEvent e -> e.getLocation() != null ? e.getLocation() : senderLocation(e.getSender());
            case WorldEvent _, WeatherEvent _, ServerEvent _, ServerTickStartEvent _, ServerTickEndEvent _ -> null;
            default -> this.fields.location(event).orElse(null);
        };
        return Optional.ofNullable(location);
    }

    private static @Nullable Player clicker(PlayerCustomClickEvent event) {
        return event.getCommonConnection() instanceof PlayerGameConnection connection ? connection.getPlayer() : null;
    }

}
