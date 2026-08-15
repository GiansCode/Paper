package io.papermc.paper.niceserver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityRemoveEvent;

/**
 * Fork-local knobs that are not part of upstream Paper.
 * Loaded from {@code niceserver.yml} in the server directory.
 */
public final class NiceServerConfig {

    private static final String HEADER = """
        NiceServer configuration (this Paper fork)

        Safe-by-default performance and bugfix toggles. /niceserver reload applies most
        options on the next tick. Set an option to false / -1 for closer vanilla/Paper behavior.
        """;

    private static File configFile;
    private static YamlConfiguration config;

    // --- fixes ---
    public static boolean skipEntitySaveOnUnloadWithoutSave = true;
    public static boolean skipAntiXrayOnBadPalette = true;

    // --- optimizations ---
    public static boolean skipEventsWithNoListeners = true;
    public static boolean skipZeroMovement = true;
    public static boolean preventMovingIntoUnloadedChunks = true;
    public static boolean reduceProjectileChunkLoading = true;
    public static boolean disablePrecatureSpawnEvent = false;
    public static boolean skipGenericGameEventIfNoListeners = true;
    public static boolean skipPlayerCommandSendEventIfNoListeners = true;
    public static int inactiveGoalSelectorInterval = 20;
    public static boolean throttleHopperWhenFull = true;
    public static int hopperThrottleSkipTicks = 8;
    public static int itemMergeInterval = 2;
    public static boolean skipMapUpdatesWithoutVanillaRenderer = true;
    public static boolean skipMarkerArmorStandTick = true;
    public static boolean useVirtualThreadsForAsyncScheduler = true;
    public static boolean asyncPathfinding = true;
    public static int asyncPathfindingThreads = 0;
    public static boolean asyncMobSpawning = true;
    public static boolean dynamicActivationOfBrain = true;
    public static int dabStartDistanceSquared = 8 * 8;
    public static int dabActivationDistanceMod = 7;
    public static int dabMaximumActivationPrio = 20;
    public static boolean dabDontEnableIfInWater = true;
    public static int targetGoalInterval = 10;
    public static boolean skipUnloadedChunksForPhantoms = true;
    public static boolean optimizeSuffocation = true;
    public static boolean optimizeChunkLightning = true;
    public static boolean cacheSpecialDates = true;
    public static boolean skipUnloadedChunksForEndermanTeleport = true;
    public static boolean skipUnloadedChunksForBlockGoals = true;

    // --- gameplay ---
    public static boolean disableNaturalMobSpawning = false;
    public static boolean disableItemMerging = false;
    public static int playerArrowDespawnRate = -1;
    public static int maxPlayerArrows = -1;
    public static int projectileMaxLifetime = -1;
    public static int enderpearlMaxLifetime = -1;
    public static boolean disableLeafDecay = false;
    public static boolean disableGrassSpread = false;
    public static boolean disableCropGrowth = false;
    public static boolean disableVillagerBreeding = false;
    public static boolean disableNetherPortalSpawn = false;
    public static boolean disableSculkSpreading = false;

    private NiceServerConfig() {
    }

    public static void init() {
        init(new File("niceserver.yml"));
    }

    public static void init(final File file) {
        configFile = file;
        config = new YamlConfiguration();
        try {
            if (file.exists()) {
                config.load(file);
            }
        } catch (final IOException ignored) {
        } catch (final InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load niceserver.yml, please correct your syntax errors", ex);
            throw new RuntimeException(ex);
        }

        config.options().setHeader(List.of(HEADER.split("\n")));
        config.options().copyDefaults(true);
        config.options().parseComments(true);

        skipEntitySaveOnUnloadWithoutSave = getBoolean("fixes.skip-entity-save-on-unload-without-save", true,
            "Honor World.unloadChunk(..., save=false) and do not write entities for that chunk.");
        skipAntiXrayOnBadPalette = getBoolean("fixes.skip-anti-xray-on-bad-palette", true,
            "Skip Anti-Xray for a section instead of crashing when a palette is unexpectedly resized.");

        skipEventsWithNoListeners = getBoolean("optimizations.skip-events-with-no-listeners", true,
            "Do not allocate/dispatch Bukkit events when no plugin is listening.");
        skipZeroMovement = getBoolean("optimizations.skip-zero-movement", true,
            "Skip Entity.move when the delta is (0,0,0) and the bounding box did not change.");
        preventMovingIntoUnloadedChunks = getBoolean("optimizations.prevent-moving-into-unloaded-chunks", true,
            "Block non-player entities from moving into unloaded chunks (players are never blocked).");
        reduceProjectileChunkLoading = getBoolean("optimizations.reduce-projectile-chunk-loading", true,
            "Projectiles do not load chunks by flying into them.");
        disablePrecatureSpawnEvent = getBoolean("optimizations.disable-precature-spawn-event", false,
            "Never fire PreCreatureSpawnEvent / PreSpawnerSpawnEvent. Faster spawning; plugins cannot cancel.");
        skipGenericGameEventIfNoListeners = getBoolean("optimizations.skip-generic-game-event-if-no-listeners", true,
            "Skip GenericGameEvent when nothing is listening.");
        skipPlayerCommandSendEventIfNoListeners = getBoolean("optimizations.skip-player-command-send-event-if-no-listeners", true,
            "Skip PlayerCommandSendEvent when nothing is listening.");
        inactiveGoalSelectorInterval = Math.max(1, getInt("optimizations.inactive-goal-selector-interval", 20,
            "How often inactive mob goal selectors run, in ticks. Paper is 3. 20 is Leaf/Pufferfish-style."));
        throttleHopperWhenFull = getBoolean("optimizations.throttle-hopper-when-full", true,
            "When a hopper transfer fails (full destination), wait hopper-throttle-skip-ticks instead of retrying next tick.");
        hopperThrottleSkipTicks = Math.max(1, getInt("optimizations.hopper-throttle-skip-ticks", 8,
            "Cooldown applied when throttle-hopper-when-full is true. 8 matches vanilla hopper cooldown."));
        itemMergeInterval = Math.max(1, getInt("optimizations.item-merge-interval", 2,
            "Only search for item merges every N ticks. 1 is vanilla. 2-4 helps item dumps."));
        skipMapUpdatesWithoutVanillaRenderer = getBoolean("optimizations.skip-map-updates-without-vanilla-renderer", true,
            "Skip vanilla map terrain scanning for plugin maps that replaced CraftMapRenderer.");
        skipMarkerArmorStandTick = getBoolean("optimizations.skip-marker-armor-stand-tick", true,
            "Marker armor stands skip LivingEntity.tick. Paper entities.armor-stands.tick still applies.");
        useVirtualThreadsForAsyncScheduler = getBoolean("optimizations.use-virtual-threads-for-async-scheduler", true,
            "Run Folia/Paper async scheduler tasks on virtual threads. Restart required.");
        asyncPathfinding = getBoolean("optimizations.async-pathfinding", true,
            "Run mob A* pathfinding off the main thread. Paths apply 1 tick later. Restart not required to disable.");
        asyncPathfindingThreads = Math.max(0, getInt("optimizations.async-pathfinding-threads", 0,
            "Worker threads for async pathfinding. 0 = CPU cores / 4. Restart required to change."));
        asyncMobSpawning = getBoolean("optimizations.async-mob-spawning", true,
            "Recount mob caps off-thread. Spawning still happens on the main thread. Needs paper per-player-mob-spawns.");
        dynamicActivationOfBrain = getBoolean("optimizations.dynamic-activation-of-brain", true,
            "Throttle distant mob AI (Leaf/Pufferfish DAB). Nearby mobs still tick every tick.");
        final int dabStartDistance = Math.max(1, getInt("optimizations.dab-start-distance", 8,
            "Distance in blocks before DAB starts throttling AI."));
        dabStartDistanceSquared = dabStartDistance * dabStartDistance;
        dabActivationDistanceMod = Math.max(1, getInt("optimizations.dab-activation-distance-mod", 7,
            "Bit-shift for DAB tick skip. Higher = more throttling. Leaf default 7."));
        dabMaximumActivationPrio = Math.max(1, getInt("optimizations.dab-maximum-activation-prio", 20,
            "Max ticks between AI updates for very distant mobs."));
        dabDontEnableIfInWater = getBoolean("optimizations.dab-dont-enable-if-in-water", true,
            "Do not DAB-throttle drowning land mobs.");
        targetGoalInterval = Math.max(0, getInt("optimizations.target-goal-interval", 10,
            "Replace cheap vanilla target intervals (<=10) with this many ticks. 0 keeps vanilla."));
        skipUnloadedChunksForPhantoms = getBoolean("optimizations.skip-unloaded-chunks-for-phantoms", true,
            "Do not load chunks just to spawn phantoms.");
        optimizeSuffocation = getBoolean("optimizations.optimize-suffocation", true,
            "Check isInWall at most every 10 ticks, and skip if invulnerable. Wither is unchanged. Pufferfish.");
        optimizeChunkLightning = getBoolean("optimizations.optimize-chunk-lightning", true,
            "Replace per-tick thunder RNG with a per-chunk countdown. Same rate, cheaper. Airplane/Pufferfish.");
        cacheSpecialDates = getBoolean("optimizations.cache-special-dates", true,
            "Cache Halloween/Christmas calendar checks for 1 hour. Airplane/Pufferfish.");
        skipUnloadedChunksForEndermanTeleport = getBoolean("optimizations.skip-unloaded-chunks-for-enderman-teleport", true,
            "Enderman random teleport does not load chunks. Airplane/Pufferfish.");
        skipUnloadedChunksForBlockGoals = getBoolean("optimizations.skip-unloaded-chunks-for-block-goals", true,
            "MoveToBlockGoal skips unloaded chunks (Paper#6045). Pufferfish.");

        disableNaturalMobSpawning = getBoolean("gameplay.disable-natural-mob-spawning", false,
            "Disable natural (chunk) mob spawning. Spawners, eggs, and plugins still work.");
        disableItemMerging = getBoolean("gameplay.disable-item-merging", false,
            "Dropped items never merge.");
        playerArrowDespawnRate = getInt("gameplay.player-arrow-despawn-rate", -1,
            "Ticks until player-owned arrows despawn. -1 uses spigot.yml arrow-despawn-rate.");
        maxPlayerArrows = getInt("gameplay.max-player-arrows", -1,
            "Max in-world arrows per player. Oldest is removed when exceeded. -1 disables. Tridents are ignored.");
        projectileMaxLifetime = getInt("gameplay.projectile-max-lifetime", -1,
            "Despawn any projectile (including pearls unless enderpearl-max-lifetime is set) after this many ticks. -1 disables.");
        enderpearlMaxLifetime = getInt("gameplay.enderpearl-max-lifetime", -1,
            "Despawn ender pearls after this many ticks. -1 disables. Overrides projectile-max-lifetime for pearls.");
        disableLeafDecay = getBoolean("gameplay.disable-leaf-decay", false,
            "Leaves never decay.");
        disableGrassSpread = getBoolean("gameplay.disable-grass-spread", false,
            "Grass and mycelium do not spread.");
        disableCropGrowth = getBoolean("gameplay.disable-crop-growth", false,
            "Crops do not age from random ticks. Bonemeal still works.");
        disableVillagerBreeding = getBoolean("gameplay.disable-villager-breeding", false,
            "Villagers do not breed.");
        disableNetherPortalSpawn = getBoolean("gameplay.disable-nether-portal-spawn", false,
            "Zombified piglins do not spawn from nether portals.");
        disableSculkSpreading = getBoolean("gameplay.disable-sculk-spreading", false,
            "Sculk catalysts do not spread sculk.");

        try {
            config.save(configFile);
        } catch (final IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + configFile, ex);
        }
    }

    public static boolean shouldCall(final HandlerList handlers) {
        return !skipEventsWithNoListeners || handlers.hasRegisteredListeners();
    }

    public static boolean shouldThrottleDistantBrain(final net.minecraft.world.entity.Mob mob) {
        if (!dynamicActivationOfBrain) {
            return false;
        }
        final int prio = mob.activatedPriority;
        return prio > 1 && (mob.tickCount % prio) != 0;
    }

    public static boolean tryDespawnProjectile(final net.minecraft.world.entity.projectile.Projectile projectile) {
        if (projectileMaxLifetime <= 0 && enderpearlMaxLifetime <= 0) {
            return false;
        }
        final int maxLife = projectile instanceof net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
            ? (enderpearlMaxLifetime > 0 ? enderpearlMaxLifetime : projectileMaxLifetime)
            : projectileMaxLifetime;
        if (maxLife <= 0 || projectile.tickCount < maxLife || projectile.level().isClientSide()) {
            return false;
        }
        projectile.discard(EntityRemoveEvent.Cause.DESPAWN);
        return true;
    }

    public static void limitPlayerArrows(final AbstractArrow arrow) {
        if (maxPlayerArrows <= 0 || arrow.level().isClientSide() || arrow instanceof ThrownTrident) {
            return;
        }
        if (!(arrow.getOwner() instanceof final Player player) || arrow.pickup != AbstractArrow.Pickup.ALLOWED) {
            return;
        }

        if (!(player.level() instanceof final net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        int extra = 0;
        AbstractArrow oldest = null;
        for (final AbstractArrow other : serverLevel.getEntities(
            net.minecraft.world.level.entity.EntityTypeTest.forClass(AbstractArrow.class),
            player.getBoundingBox().inflate(384.0),
            candidate -> candidate != arrow
                && !(candidate instanceof ThrownTrident)
                && candidate.getOwner() == player
                && candidate.pickup == AbstractArrow.Pickup.ALLOWED
        )) {
            extra++;
            if (oldest == null || other.tickCount > oldest.tickCount) {
                oldest = other;
            }
        }
        if (extra >= maxPlayerArrows && oldest != null) {
            oldest.discard(EntityRemoveEvent.Cause.DESPAWN);
        }
    }

    private static boolean getBoolean(final String path, final boolean def, final String comment) {
        config.addDefault(path, def);
        config.setComments(path, List.of(comment));
        return config.getBoolean(path, def);
    }

    private static int getInt(final String path, final int def, final String comment) {
        config.addDefault(path, def);
        config.setComments(path, List.of(comment));
        return config.getInt(path, def);
    }
}
