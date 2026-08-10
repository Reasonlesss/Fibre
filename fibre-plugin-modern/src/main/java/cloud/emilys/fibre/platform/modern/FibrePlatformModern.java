package cloud.emilys.fibre.platform.modern;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.features.adventure.AdventureFeature;
import cloud.emilys.fibre.features.event.EventFieldAccessor;
import cloud.emilys.fibre.platform.FibrePlatform;
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
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.audience.Audience;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
public final class FibrePlatformModern implements FibrePlatform {

    private final EventFieldAccessor fieldAccessor = new EventFieldAccessor();

    @Override
    public void install(Fibre fibre) {
        fibre.getStartupRegistry().registerTypeFinder(Event.class, World.class, this::findWorld);
        fibre.getStartupRegistry().registerTypeFinder(Event.class, Entity.class, this::findEntity);
        fibre.getStartupRegistry().registerTypeFinder(Event.class, Player.class, this::findPlayer);
        AdventureFeature.install(fibre, player -> (Audience) player);
    }

    @SuppressWarnings({"deprecation", "removal"})
    private Optional<Player> findPlayer(Event event) {
        Objects.requireNonNull(event, "event");
        Player player =
                switch (event) {
                    case PlayerEvent e -> e.getPlayer();
                    case BlockBreakEvent e -> e.getPlayer();
                    case BlockCanBuildEvent e -> e.getPlayer();
                    case BlockDamageAbortEvent e -> e.getPlayer();
                    case BlockDamageEvent e -> e.getPlayer();
                    case BlockDispenseArmorEvent e -> this.asPlayer(e.getTargetEntity());
                    case BlockDispenseLootEvent e -> e.getPlayer();
                    case BlockDropItemEvent e -> e.getPlayer();
                    case BlockFertilizeEvent e -> e.getPlayer();
                    case BlockIgniteEvent e -> e.getPlayer();
                    case BlockPlaceEvent e -> e.getPlayer();
                    case BlockReceiveGameEvent e -> this.asPlayer(e.getEntity());
                    case BlockShearEntityEvent e -> this.asPlayer(e.getEntity());
                    case CauldronLevelChangeEvent e -> this.asPlayer(e.getEntity());
                    case EntityBlockFormEvent e -> this.asPlayer(e.getEntity());
                    case SignChangeEvent e -> e.getPlayer();
                    case TNTPrimeEvent e -> this.asPlayer(e.getPrimingEntity());
                    case org.bukkit.event.block.BellRingEvent e -> this.asPlayer(e.getEntity());
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
                    case BellRevealRaiderEvent e -> this.asPlayer(e.getEntity());
                    case BlockBreakProgressUpdateEvent e -> this.asPlayer(e.getEntity());
                    case com.destroystokyo.paper.event.block.TNTPrimeEvent e -> this.asPlayer(e.getPrimerEntity());
                    case EntityDyeEvent e -> e.getPlayer();
                    case ItemCraftedEvent e -> e.getPlayer();
                    case PlayerChunkLoadEvent e -> e.getPlayer();
                    case PlayerChunkUnloadEvent e -> e.getPlayer();
                    case PlayerCustomClickEvent e ->
                        e.getCommonConnection() instanceof PlayerGameConnection connection
                                ? connection.getPlayer()
                                : null;
                    case InventoryEvent e ->
                        e.getViewers().stream()
                                .filter(Player.class::isInstance)
                                .map(Player.class::cast)
                                .findFirst()
                                .orElseGet(() -> this.findInventoryPlayer(e.getInventory()));
                    case InventoryMoveItemEvent e -> this.findInventoryPlayer(e.getInitiator());
                    case UnknownCommandEvent e -> this.asPlayer(e.getSender());
                    case ServerCommandEvent e -> this.asPlayer(e.getSender());
                    case TabCompleteEvent e -> this.asPlayer(e.getSender());
                    case AsyncTabCompleteEvent e -> this.asPlayer(e.getSender());
                    case EntityEvent e -> this.asPlayer(e.getEntity());
                    case BlockEvent ignored -> null;
                    case HangingEvent ignored -> null;
                    case VehicleEvent e -> this.asPlayer(e.getVehicle());
                    case WorldEvent ignored -> null;
                    case WeatherEvent ignored -> null;
                    case ServerEvent ignored -> null;
                    case ServerTickStartEvent ignored -> null;
                    case ServerTickEndEvent ignored -> null;
                    default ->
                        this.isCustomEvent(event)
                                ? this.fieldAccessor.get(event, Player.class).orElse(null)
                                : null;
                };
        return Optional.ofNullable(player);
    }

    @SuppressWarnings({"deprecation", "removal"})
    private Optional<Entity> findEntity(Event event) {
        Objects.requireNonNull(event, "event");
        Entity entity =
                switch (event) {
                    case EntityEvent e -> e.getEntity();
                    case PlayerEvent e -> e.getPlayer();
                    case HangingEvent e -> e.getEntity();
                    case InventoryEvent e ->
                        e.getViewers().stream()
                                .map(Entity.class::cast)
                                .findFirst()
                                .orElseGet(() -> this.findInventoryEntity(e.getInventory()));
                    case BlockBreakEvent e -> e.getPlayer();
                    case BlockCanBuildEvent e -> e.getPlayer();
                    case BlockDamageAbortEvent e -> e.getPlayer();
                    case BlockDamageEvent e -> e.getPlayer();
                    case BlockDispenseArmorEvent e -> e.getTargetEntity();
                    case BlockDispenseLootEvent e -> e.getPlayer();
                    case BlockDropItemEvent e -> e.getPlayer();
                    case BlockFertilizeEvent e -> e.getPlayer();
                    case BlockIgniteEvent e -> e.getIgnitingEntity();
                    case BlockPlaceEvent e -> e.getPlayer();
                    case BlockReceiveGameEvent e -> e.getEntity();
                    case BlockShearEntityEvent e -> e.getEntity();
                    case CauldronLevelChangeEvent e -> e.getEntity();
                    case EntityBlockFormEvent e -> e.getEntity();
                    case SignChangeEvent e -> e.getPlayer();
                    case TNTPrimeEvent e -> e.getPrimingEntity();
                    case org.bukkit.event.block.BellRingEvent e -> e.getEntity();
                    case BellRevealRaiderEvent e -> e.getEntity();
                    case BlockBreakProgressUpdateEvent e -> e.getEntity();
                    case BeaconEffectEvent e -> e.getPlayer();
                    case BlockLockCheckEvent e -> e.getPlayer();
                    case VaultChangeStateEvent e -> e.getPlayer();
                    case com.destroystokyo.paper.event.block.TNTPrimeEvent e -> e.getPrimerEntity();
                    case FurnaceExtractEvent e -> e.getPlayer();
                    case RaidTriggerEvent e -> e.getPlayer();
                    case StructureGrowEvent e -> e.getPlayer();
                    case PlayerChunkLoadEvent e -> e.getPlayer();
                    case PlayerChunkUnloadEvent e -> e.getPlayer();
                    case PlayerLeashEntityEvent e -> e.getEntity();
                    case InventoryMoveItemEvent e -> this.findInventoryEntity(e.getInitiator());
                    case InventoryPickupItemEvent e -> e.getItem();
                    case ItemCraftedEvent e -> e.getPlayer();
                    case ThrownEggHatchEvent e -> e.getEgg();
                    case PlayerCustomClickEvent e ->
                        e.getCommonConnection() instanceof PlayerGameConnection connection
                                ? connection.getPlayer()
                                : null;
                    case UnknownCommandEvent e -> this.asEntity(e.getSender());
                    case ServerCommandEvent e -> this.asEntity(e.getSender());
                    case TabCompleteEvent e -> this.asEntity(e.getSender());
                    case AsyncTabCompleteEvent e -> this.asEntity(e.getSender());
                    case LightningStrikeEvent e -> e.getLightning();
                    case VehicleEvent e -> e.getVehicle();
                    case BlockEvent ignored -> null;
                    case WorldEvent ignored -> null;
                    case WeatherEvent ignored -> null;
                    case ServerEvent ignored -> null;
                    case ServerTickStartEvent ignored -> null;
                    case ServerTickEndEvent ignored -> null;
                    default ->
                        this.isCustomEvent(event)
                                ? this.fieldAccessor.get(event, Entity.class).orElse(null)
                                : null;
                };
        return Optional.ofNullable(entity);
    }

    private Optional<World> findWorld(Event event) {
        Objects.requireNonNull(event, "event");
        World world =
                switch (event) {
                    case WorldEvent e -> e.getWorld();
                    case EntityEvent e -> e.getEntity().getWorld();
                    case PlayerEvent e -> e.getPlayer().getWorld();
                    case BlockEvent e -> e.getBlock().getWorld();
                    case HangingEvent e -> e.getEntity().getWorld();
                    case InventoryEvent e ->
                        e.getViewers().stream()
                                .findFirst()
                                .map(HumanEntity::getWorld)
                                .orElseGet(() -> this.findInventoryWorld(e.getInventory()));
                    case PlayerLeashEntityEvent e -> e.getPlayer().getWorld();
                    case InventoryMoveItemEvent e -> this.findInventoryWorld(e.getInitiator());
                    case InventoryPickupItemEvent e -> e.getItem().getWorld();
                    case ItemCraftedEvent e -> e.getPlayer().getWorld();
                    case ThrownEggHatchEvent e -> e.getEgg().getWorld();
                    case PreCreatureSpawnEvent e -> e.getSpawnLocation().getWorld();
                    case AsyncPlayerSpawnLocationEvent e -> e.getSpawnLocation().getWorld();
                    case PlayerCustomClickEvent e ->
                        e.getCommonConnection() instanceof PlayerGameConnection connection
                                ? connection.getPlayer().getWorld()
                                : null;
                    case UnknownCommandEvent e ->
                        e.getCommandSource().getLocation().getWorld();
                    case ServerCommandEvent e -> this.findSenderWorld(e.getSender());
                    case TabCompleteEvent e ->
                        e.getLocation() == null
                                ? this.findSenderWorld(e.getSender())
                                : e.getLocation().getWorld();
                    case AsyncTabCompleteEvent e ->
                        e.getLocation() == null
                                ? this.findSenderWorld(e.getSender())
                                : e.getLocation().getWorld();
                    case MapInitializeEvent e -> e.getMap().getWorld();
                    case VehicleEvent e -> e.getVehicle().getWorld();
                    case WeatherEvent e -> e.getWorld();
                    case ServerEvent ignored -> null;
                    case ServerTickStartEvent ignored -> null;
                    case ServerTickEndEvent ignored -> null;
                    default ->
                        this.isCustomEvent(event)
                                ? this.fieldAccessor.get(event, World.class).orElse(null)
                                : null;
                };
        return Optional.ofNullable(world);
    }

    private @Nullable Player findInventoryPlayer(Inventory inventory) {
        return inventory.getViewers().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .findFirst()
                .orElseGet(() -> this.asPlayer(inventory.getHolder()));
    }

    private @Nullable Entity findInventoryEntity(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        return holder instanceof Entity entity
                ? entity
                : inventory.getViewers().stream().findFirst().orElse(null);
    }

    private @Nullable World findInventoryWorld(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        return switch (holder) {
            case Entity entity -> entity.getWorld();
            case BlockState state -> state.getWorld();
            case DoubleChest chest -> chest.getWorld();
            case null, default ->
                inventory.getViewers().stream()
                        .findFirst()
                        .map(HumanEntity::getWorld)
                        .orElse(null);
        };
    }

    private @Nullable World findSenderWorld(CommandSender sender) {
        return switch (sender) {
            case Entity entity -> entity.getWorld();
            case BlockCommandSender blockSender -> blockSender.getBlock().getWorld();
            default -> null;
        };
    }

    private @Nullable Player asPlayer(@Nullable Object object) {
        return object instanceof Player player ? player : null;
    }

    private @Nullable Entity asEntity(Object object) {
        return object instanceof Entity entity ? entity : null;
    }

    private boolean isCustomEvent(Event event) {
        String packageName = event.getClass().getPackageName();
        return !packageName.startsWith("org.bukkit.")
                && !packageName.startsWith("org.spigotmc.")
                && !packageName.startsWith("io.papermc.paper.")
                && !packageName.startsWith("com.destroystokyo.paper.");
    }
}
