package cloud.emilys.fibre.platform.legacy;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.features.event.EventFieldAccessor;
import cloud.emilys.fibre.platform.FibrePlatform;
import java.util.Objects;
import java.util.Optional;
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
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
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
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibrePlatformLegacy implements FibrePlatform {

    private final EventFieldAccessor fieldAccessor = new EventFieldAccessor();

    @Override
    public void install(Fibre fibre) {
        fibre.getStartupRegistry().registerTypeFinder(Event.class, World.class, this::findWorld);
        fibre.getStartupRegistry().registerTypeFinder(Event.class, Entity.class, this::findEntity);
        fibre.getStartupRegistry().registerTypeFinder(Event.class, Player.class, this::findPlayer);
        try {
            Class.forName("net.kyori.adventure.audience.Audience");
            FibreAdventureLegacy.install(fibre);
        } catch (ClassNotFoundException ignored) {
        }
    }

    @SuppressWarnings("deprecation")
    private Optional<Player> findPlayer(Event event) {
        Objects.requireNonNull(event, "event");
        Player player =
                switch (event) {
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
                    case InventoryEvent e ->
                        e.getViewers().stream()
                                .filter(Player.class::isInstance)
                                .map(Player.class::cast)
                                .findFirst()
                                .orElseGet(() -> this.findInventoryPlayer(e.getInventory()));
                    case InventoryMoveItemEvent e -> this.findInventoryPlayer(e.getInitiator());
                    case ServerCommandEvent e -> this.asPlayer(e.getSender());
                    case EntityEvent e -> this.asPlayer(e.getEntity());
                    case BlockEvent ignored -> null;
                    case HangingEvent ignored -> null;
                    case PaintingEvent ignored -> null;
                    case VehicleEvent e -> this.asPlayer(e.getVehicle());
                    case WorldEvent ignored -> null;
                    case WeatherEvent ignored -> null;
                    case ServerEvent ignored -> null;
                    default ->
                        this.isCustomEvent(event)
                                ? this.fieldAccessor.get(event, Player.class).orElse(null)
                                : null;
                };
        return Optional.ofNullable(player);
    }

    @SuppressWarnings("deprecation")
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
                    case BlockDamageEvent e -> e.getPlayer();
                    case BlockIgniteEvent e -> e.getIgnitingEntity();
                    case BlockPlaceEvent e -> e.getPlayer();
                    case EntityBlockFormEvent e -> e.getEntity();
                    case SignChangeEvent e -> e.getPlayer();
                    case FurnaceExtractEvent e -> e.getPlayer();
                    case StructureGrowEvent e -> e.getPlayer();
                    case PlayerLeashEntityEvent e -> e.getEntity();
                    case InventoryMoveItemEvent e -> this.findInventoryEntity(e.getInitiator());
                    case InventoryPickupItemEvent e -> e.getItem();
                    case ServerCommandEvent e -> this.asEntity(e.getSender());
                    case PaintingEvent e -> e.getPainting();
                    case LightningStrikeEvent e -> e.getLightning();
                    case VehicleEvent e -> e.getVehicle();
                    case BlockEvent ignored -> null;
                    case WorldEvent ignored -> null;
                    case WeatherEvent ignored -> null;
                    case ServerEvent ignored -> null;
                    default ->
                        this.isCustomEvent(event)
                                ? this.fieldAccessor.get(event, Entity.class).orElse(null)
                                : null;
                };
        return Optional.ofNullable(entity);
    }

    @SuppressWarnings("deprecation")
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
                    case ServerCommandEvent e -> this.findSenderWorld(e.getSender());
                    case MapInitializeEvent e -> e.getMap().getWorld();
                    case PaintingEvent e -> e.getPainting().getWorld();
                    case VehicleEvent e -> e.getVehicle().getWorld();
                    case WeatherEvent e -> e.getWorld();
                    case ServerEvent ignored -> null;
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
        return !packageName.startsWith("org.bukkit.") && !packageName.startsWith("org.spigotmc.");
    }
}
