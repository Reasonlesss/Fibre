package codes.reason.fibre.bukkit.legacy;

import codes.reason.fibre.platform.PlayerStates;
import codes.reason.fibre.player.PlayerAdapter;
import codes.reason.fibre.player.PlayerSnapshot;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// each part captures, applies and resets its own state, and they're applied in an order that keeps them valid
@NullMarked
final class LegacyPlayerStates implements PlayerStates {

    @Override
    public PlayerSnapshot capture(PlayerAdapter<?> adapter) {
        Player player = player(adapter);
        // puts an item held on the cursor back into the inventory, so it's captured too
        player.closeInventory();
        return new Snapshot(
                Place.capture(player),
                Items.capture(player),
                List.copyOf(player.getActivePotionEffects()),
                Stats.capture(player),
                Progress.capture(player),
                Movement.capture(player),
                Display.capture(player));
    }

    @Override
    public void restore(PlayerAdapter<?> adapter, PlayerSnapshot snapshot) {
        if (!(snapshot instanceof Snapshot state)) {
            throw new IllegalArgumentException("%s wasn't captured by Bukkit".formatted(snapshot));
        }
        Player player = player(adapter);
        resetTransient(player);
        state.place().apply(player);
        state.items().apply(player);
        clearEffects(player);
        player.addPotionEffects(state.effects());
        state.stats().apply(player);
        state.progress().apply(player);
        state.movement().apply(player);
        state.display().apply(player);
        player.updateInventory();
    }

    @Override
    public void reset(PlayerAdapter<?> adapter) {
        Player player = player(adapter);
        resetTransient(player);
        Place.reset(player);
        Items.reset(player);
        clearEffects(player);
        Stats.reset(player);
        Progress.reset(player);
        Movement.reset(player);
        Display.reset(player);
        player.updateInventory();
    }

    // things that only matter in the moment, so they're cleared but never captured
    @SuppressWarnings("deprecation")
    private static void resetTransient(Player player) {
        player.closeInventory();
        player.leaveVehicle();
        player.eject();
        player.setVelocity(new Vector());
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setSpectatorTarget(null);
        }
        player.setSneaking(false);
        player.setSprinting(false);
        player.setNoDamageTicks(0);
        player.setLastDamage(0);
        player.resetTitle();
    }

    private static void clearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
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
            List<PotionEffect> effects,
            Stats stats,
            Progress progress,
            Movement movement,
            Display display) implements PlayerSnapshot {
    }

    private record Place(Location location, @Nullable Location bed, Location compass) {

        static Place capture(Player player) {
            return new Place(player.getLocation(), player.getBedSpawnLocation(), player.getCompassTarget());
        }

        static void reset(Player player) {
            // where they stand is up to the game
            player.setBedSpawnLocation(null);
            player.setCompassTarget(player.getWorld().getSpawnLocation());
        }

        void apply(Player player) {
            // first of all, since changing world can change other state; skipped if the world's gone
            if (Bukkit.getWorld(this.location.getWorld().getUID()) != null) {
                player.teleport(this.location);
            }
            player.setBedSpawnLocation(this.bed, true);
            player.setCompassTarget(this.compass);
        }
    }

    // 1.8's contents are only the main inventory, so armour is kept apart
    private record Items(@Nullable ItemStack[] contents, @Nullable ItemStack[] armor, @Nullable ItemStack[] enderChest) {

        static Items capture(Player player) {
            return new Items(
                    copy(player.getInventory().getContents()),
                    copy(player.getInventory().getArmorContents()),
                    copy(player.getEnderChest().getContents()));
        }

        static void reset(Player player) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getEnderChest().clear();
        }

        void apply(Player player) {
            player.getInventory().setContents(copy(this.contents));
            player.getInventory().setArmorContents(copy(this.armor));
            player.getEnderChest().setContents(copy(this.enderChest));
        }
    }

    private record Stats(
            double health,
            double maxHealth,
            int food,
            float saturation,
            float exhaustion,
            int fireTicks,
            float fallDistance,
            int air,
            int maxAir,
            int maxNoDamageTicks) {

        @SuppressWarnings("deprecation")
        static Stats capture(Player player) {
            return new Stats(
                    player.getHealth(),
                    player.getMaxHealth(),
                    player.getFoodLevel(),
                    player.getSaturation(),
                    player.getExhaustion(),
                    player.getFireTicks(),
                    player.getFallDistance(),
                    player.getRemainingAir(),
                    player.getMaximumAir(),
                    player.getMaximumNoDamageTicks());
        }

        @SuppressWarnings("deprecation")
        static void reset(Player player) {
            player.resetMaxHealth();
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(5);
            player.setExhaustion(0);
            player.setFireTicks(0);
            player.setFallDistance(0);
            player.setMaximumAir(300);
            player.setRemainingAir(300);
            player.setMaximumNoDamageTicks(20);
        }

        @SuppressWarnings("deprecation")
        void apply(Player player) {
            // before the health, which can't be above it
            player.setMaxHealth(this.maxHealth);
            player.setHealth(Math.min(this.health, this.maxHealth));
            player.setFoodLevel(this.food);
            player.setSaturation(this.saturation);
            player.setExhaustion(this.exhaustion);
            player.setFireTicks(this.fireTicks);
            player.setFallDistance(this.fallDistance);
            player.setMaximumAir(this.maxAir);
            player.setRemainingAir(this.air);
            player.setMaximumNoDamageTicks(this.maxNoDamageTicks);
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

    private record Movement(GameMode gameMode, boolean allowFlight, boolean flying, float walkSpeed, float flySpeed) {

        static Movement capture(Player player) {
            return new Movement(
                    player.getGameMode(),
                    player.getAllowFlight(),
                    player.isFlying(),
                    player.getWalkSpeed(),
                    player.getFlySpeed());
        }

        static void reset(Player player) {
            GameMode gameMode = Bukkit.getDefaultGameMode();
            boolean flight = gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
            new Movement(gameMode, flight, false, 0.2F, 0.1F).apply(player);
        }

        void apply(Player player) {
            player.setGameMode(this.gameMode);
            // after the game mode, which changes whether they can fly
            player.setAllowFlight(this.allowFlight);
            player.setFlying(this.allowFlight && this.flying);
            player.setWalkSpeed(this.walkSpeed);
            player.setFlySpeed(this.flySpeed);
        }
    }

    private record Display(
            String displayName,
            String listName,
            Scoreboard scoreboard,
            boolean healthScaled,
            double healthScale,
            long timeOffset,
            boolean timeRelative,
            @Nullable WeatherType weather) {

        static Display capture(Player player) {
            return new Display(
                    player.getDisplayName(),
                    player.getPlayerListName(),
                    player.getScoreboard(),
                    player.isHealthScaled(),
                    player.getHealthScale(),
                    player.getPlayerTimeOffset(),
                    player.isPlayerTimeRelative(),
                    player.getPlayerWeather());
        }

        static void reset(Player player) {
            new Display(
                    player.getName(),
                    player.getName(),
                    Bukkit.getScoreboardManager().getMainScoreboard(),
                    false,
                    20,
                    0,
                    true,
                    null).apply(player);
        }

        void apply(Player player) {
            player.setDisplayName(this.displayName);
            player.setPlayerListName(this.listName);
            player.setScoreboard(this.scoreboard);
            player.setHealthScale(this.healthScale);
            player.setHealthScaled(this.healthScaled);
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
