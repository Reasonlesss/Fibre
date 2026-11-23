package codes.reason.fibre.bukkit.paper;

import codes.reason.fibre.platform.PlayerStates;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.player.PlayerSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.WeatherType;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// each part captures, applies and resets its own state, and they're applied in an order that keeps them valid
@NullMarked
final class PaperPlayerStates implements PlayerStates {

    @Override
    public PlayerSnapshot capture(PlayerAdapter<?> adapter) {
        Player player = player(adapter);
        // puts an item held on the cursor back into the inventory, so it's captured too
        player.closeInventory();
        return new Snapshot(
                Place.capture(player),
                Items.capture(player),
                Attributes.capture(player),
                List.copyOf(player.getActivePotionEffects()),
                Stats.capture(player),
                Progress.capture(player),
                Movement.capture(player),
                Flags.capture(player),
                Display.capture(player));
    }

    @Override
    public void restore(PlayerAdapter<?> adapter, PlayerSnapshot snapshot) {
        if (!(snapshot instanceof Snapshot state)) {
            throw new IllegalArgumentException("%s wasn't captured by Paper".formatted(snapshot));
        }
        Player player = player(adapter);
        resetTransient(player);
        state.place().apply(player);
        state.items().apply(player);
        // before the stats, since max health and max absorption are attributes
        state.attributes().apply(player);
        clearEffects(player);
        player.addPotionEffects(state.effects());
        state.stats().apply(player);
        state.progress().apply(player);
        state.movement().apply(player);
        state.flags().apply(player);
        state.display().apply(player);
        player.updateInventory();
    }

    @Override
    public void reset(PlayerAdapter<?> adapter) {
        Player player = player(adapter);
        resetTransient(player);
        Place.reset(player);
        Items.reset(player);
        Attributes.reset(player);
        clearEffects(player);
        Stats.reset(player);
        Progress.reset(player);
        Movement.reset(player);
        Flags.reset(player);
        Display.reset(player);
        player.updateInventory();
    }

    // things that only matter in the moment, so they're cleared but never captured
    private static void resetTransient(Player player) {
        player.closeInventory();
        player.leaveVehicle();
        player.eject();
        player.setVelocity(new Vector());
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setSpectatorTarget(null);
        }
        if (player.isSleeping()) {
            player.wakeup(false);
        }
        player.setSneaking(false);
        player.setSprinting(false);
        player.clearActiveItem();
        player.setNoDamageTicks(0);
        player.setLastDamage(0);
        player.setKiller(null);
        player.setPortalCooldown(0);
        player.setExpCooldown(0);
        player.setArrowCooldown(0);
        player.setBeeStingerCooldown(0);
        player.setWardenWarningCooldown(0);
        player.resetCooldown();
        for (ItemType item : Registry.ITEM) {
            if (player.getCooldown(item.key()) > 0) {
                player.setCooldown(item.key(), 0);
            }
        }
        player.resetTitle();
    }

    private static void clearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private static double attribute(Player player, Attribute attribute, double fallback) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance != null ? instance.getValue() : fallback;
    }

    private static Player player(PlayerAdapter<?> adapter) {
        return adapter.as(Player.class)
                .orElseThrow(() -> new IllegalArgumentException("%s isn't a Bukkit player".formatted(adapter.username())));
    }

    // items are mutable, so a snapshot holds its own copies
    private static @Nullable ItemStack[] copy(@Nullable ItemStack[] items) {
        @Nullable ItemStack[] copy = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            copy[i] = item != null ? item.clone() : null;
        }
        return copy;
    }

    private record Snapshot(
            Place place,
            Items items,
            Attributes attributes,
            List<PotionEffect> effects,
            Stats stats,
            Progress progress,
            Movement movement,
            Flags flags,
            Display display) implements PlayerSnapshot {
    }

    private record Place(Location location, @Nullable Location respawn, Location compass) {

        static Place capture(Player player) {
            return new Place(player.getLocation(), player.getRespawnLocation(), player.getCompassTarget());
        }

        static void reset(Player player) {
            // where they stand is up to the game
            player.setRespawnLocation(null);
            player.setCompassTarget(player.getWorld().getSpawnLocation());
        }

        void apply(Player player) {
            // first of all, since changing world can change other state; skipped if the world's gone
            if (this.location.isWorldLoaded()) {
                player.teleport(this.location);
            }
            player.setRespawnLocation(this.respawn, true);
            player.setCompassTarget(this.compass);
        }
    }

    // the contents include armour and the off hand
    private record Items(@Nullable ItemStack[] contents, @Nullable ItemStack[] enderChest) {

        static Items capture(Player player) {
            return new Items(copy(player.getInventory().getContents()), copy(player.getEnderChest().getContents()));
        }

        static void reset(Player player) {
            player.getInventory().clear();
            player.getEnderChest().clear();
        }

        void apply(Player player) {
            player.getInventory().setContents(copy(this.contents));
            player.getEnderChest().setContents(copy(this.enderChest));
        }
    }

    // base values and the modifiers plugins added; vanilla's own come back with the items and effects
    private record Attributes(Map<Attribute, Value> values) {

        static Attributes capture(Player player) {
            Map<Attribute, Value> values = new LinkedHashMap<>();
            for (Attribute attribute : Registry.ATTRIBUTE) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    values.put(attribute, new Value(instance.getBaseValue(), pluginModifiers(instance)));
                }
            }
            return new Attributes(values);
        }

        static void reset(Player player) {
            for (Attribute attribute : Registry.ATTRIBUTE) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.setBaseValue(instance.getDefaultValue());
                    pluginModifiers(instance).forEach(instance::removeModifier);
                }
            }
        }

        void apply(Player player) {
            reset(player);
            this.values.forEach((attribute, value) -> {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.setBaseValue(value.base());
                    value.modifiers().forEach(instance::addModifier);
                }
            });
        }

        private static List<AttributeModifier> pluginModifiers(AttributeInstance instance) {
            List<AttributeModifier> modifiers = new ArrayList<>();
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (!modifier.getKey().getNamespace().equals("minecraft")) {
                    modifiers.add(modifier);
                }
            }
            return modifiers;
        }

        private record Value(double base, List<AttributeModifier> modifiers) {}
    }

    private record Stats(
            double health,
            double absorption,
            int food,
            float saturation,
            float exhaustion,
            int fireTicks,
            int freezeTicks,
            boolean freezeLocked,
            float fallDistance,
            int air,
            int maxAir,
            int arrowsInBody,
            int beeStingersInBody,
            int maxNoDamageTicks,
            int saturatedRegenRate,
            int unsaturatedRegenRate,
            int starvationRate) {

        static Stats capture(Player player) {
            return new Stats(
                    player.getHealth(),
                    player.getAbsorptionAmount(),
                    player.getFoodLevel(),
                    player.getSaturation(),
                    player.getExhaustion(),
                    player.getFireTicks(),
                    player.getFreezeTicks(),
                    player.isFreezeTickingLocked(),
                    player.getFallDistance(),
                    player.getRemainingAir(),
                    player.getMaximumAir(),
                    player.getArrowsInBody(),
                    player.getBeeStingersInBody(),
                    player.getMaximumNoDamageTicks(),
                    player.getSaturatedRegenRate(),
                    player.getUnsaturatedRegenRate(),
                    player.getStarvationRate());
        }

        static void reset(Player player) {
            player.setHealth(attribute(player, Attribute.MAX_HEALTH, 20));
            player.setAbsorptionAmount(0);
            player.setFoodLevel(20);
            player.setSaturation(5);
            player.setExhaustion(0);
            player.setFireTicks(0);
            player.lockFreezeTicks(false);
            player.setFreezeTicks(0);
            player.setFallDistance(0);
            player.setMaximumAir(300);
            player.setRemainingAir(300);
            player.setArrowsInBody(0, false);
            player.setBeeStingersInBody(0);
            player.setMaximumNoDamageTicks(20);
            player.setSaturatedRegenRate(10);
            player.setUnsaturatedRegenRate(80);
            player.setStarvationRate(80);
        }

        void apply(Player player) {
            player.setHealth(Math.min(this.health, attribute(player, Attribute.MAX_HEALTH, 20)));
            // after the effects, since absorption can come from them
            player.setAbsorptionAmount(Math.min(this.absorption, attribute(player, Attribute.MAX_ABSORPTION, 0)));
            player.setFoodLevel(this.food);
            player.setSaturation(this.saturation);
            player.setExhaustion(this.exhaustion);
            player.setFireTicks(this.fireTicks);
            player.lockFreezeTicks(this.freezeLocked);
            player.setFreezeTicks(this.freezeTicks);
            player.setFallDistance(this.fallDistance);
            player.setMaximumAir(this.maxAir);
            player.setRemainingAir(this.air);
            player.setArrowsInBody(this.arrowsInBody, false);
            player.setBeeStingersInBody(this.beeStingersInBody);
            player.setMaximumNoDamageTicks(this.maxNoDamageTicks);
            player.setSaturatedRegenRate(this.saturatedRegenRate);
            player.setUnsaturatedRegenRate(this.unsaturatedRegenRate);
            player.setStarvationRate(this.starvationRate);
        }
    }

    private record Progress(float exp, int level, int total) {

        static Progress capture(Player player) {
            return new Progress(player.getExp(), player.getLevel(), player.getTotalExperience());
        }

        static void reset(Player player) {
            new Progress(0, 0, 0).apply(player);
        }

        void apply(Player player) {
            player.setTotalExperience(this.total);
            player.setLevel(this.level);
            player.setExp(this.exp);
        }
    }

    private record Movement(
            GameMode gameMode,
            boolean allowFlight,
            boolean flying,
            float walkSpeed,
            float flySpeed,
            TriState flyingFallDamage) {

        static Movement capture(Player player) {
            return new Movement(
                    player.getGameMode(),
                    player.getAllowFlight(),
                    player.isFlying(),
                    player.getWalkSpeed(),
                    player.getFlySpeed(),
                    player.hasFlyingFallDamage());
        }

        static void reset(Player player) {
            GameMode gameMode = Bukkit.getDefaultGameMode();
            boolean flight = gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
            new Movement(gameMode, flight, false, 0.2F, 0.1F, TriState.NOT_SET).apply(player);
        }

        void apply(Player player) {
            player.setGameMode(this.gameMode);
            // after the game mode, which changes whether they can fly
            player.setAllowFlight(this.allowFlight);
            player.setFlying(this.allowFlight && this.flying);
            player.setWalkSpeed(this.walkSpeed);
            player.setFlySpeed(this.flySpeed);
            player.setFlyingFallDamage(this.flyingFallDamage);
        }
    }

    private record Flags(
            boolean glowing,
            boolean invisible,
            boolean invulnerable,
            boolean silent,
            boolean gravity,
            boolean collidable,
            Set<UUID> collidableExemptions,
            TriState visualFire) {

        static Flags capture(Player player) {
            return new Flags(
                    player.isGlowing(),
                    player.isInvisible(),
                    player.isInvulnerable(),
                    player.isSilent(),
                    player.hasGravity(),
                    player.isCollidable(),
                    Set.copyOf(player.getCollidableExemptions()),
                    player.getVisualFire());
        }

        static void reset(Player player) {
            new Flags(false, false, false, false, true, true, Set.of(), TriState.NOT_SET).apply(player);
        }

        void apply(Player player) {
            player.setGlowing(this.glowing);
            player.setInvisible(this.invisible);
            player.setInvulnerable(this.invulnerable);
            player.setSilent(this.silent);
            player.setGravity(this.gravity);
            player.setCollidable(this.collidable);
            player.getCollidableExemptions().clear();
            player.getCollidableExemptions().addAll(this.collidableExemptions);
            player.setVisualFire(this.visualFire);
        }
    }

    private record Display(
            @Nullable Component displayName,
            @Nullable Component listName,
            Component listHeader,
            Component listFooter,
            Scoreboard scoreboard,
            List<BossBar> bossBars,
            boolean healthScaled,
            double healthScale,
            @Nullable WorldBorder worldBorder,
            long timeOffset,
            boolean timeRelative,
            @Nullable WeatherType weather) {

        static Display capture(Player player) {
            List<BossBar> bossBars = new ArrayList<>();
            player.activeBossBars().forEach(bossBars::add);
            Component header = player.playerListHeader();
            Component footer = player.playerListFooter();
            return new Display(
                    player.displayName(),
                    player.playerListName(),
                    header != null ? header : Component.empty(),
                    footer != null ? footer : Component.empty(),
                    player.getScoreboard(),
                    List.copyOf(bossBars),
                    player.isHealthScaled(),
                    player.getHealthScale(),
                    player.getWorldBorder(),
                    player.getPlayerTimeOffset(),
                    player.isPlayerTimeRelative(),
                    player.getPlayerWeather());
        }

        static void reset(Player player) {
            new Display(
                    null,
                    null,
                    Component.empty(),
                    Component.empty(),
                    Bukkit.getScoreboardManager().getMainScoreboard(),
                    List.of(),
                    false,
                    20,
                    null,
                    0,
                    true,
                    null).apply(player);
        }

        void apply(Player player) {
            // null puts the names back to the player's own
            player.displayName(this.displayName);
            player.playerListName(this.listName);
            player.sendPlayerListHeaderAndFooter(this.listHeader, this.listFooter);
            player.setScoreboard(this.scoreboard);
            List<BossBar> active = new ArrayList<>();
            player.activeBossBars().forEach(active::add);
            active.forEach(player::hideBossBar);
            this.bossBars.forEach(player::showBossBar);
            player.setHealthScale(this.healthScale);
            player.setHealthScaled(this.healthScaled);
            player.setWorldBorder(this.worldBorder);
            if (this.timeRelative && this.timeOffset == 0) {
                player.resetPlayerTime();
            } else {
                player.setPlayerTime(this.timeOffset, this.timeRelative);
            }
            if (this.weather == null) {
                player.resetPlayerWeather();
            } else {
                player.setPlayerWeather(this.weather);
            }
        }
    }

}
