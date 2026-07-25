package com.blaze.wildkits.arena;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.region.CuboidRegion;
import com.blaze.wildkits.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArenaManager {

    public static final String SPAWN_TOOL_NAME = "<gradient:#FF4500:#FFD700><bold>Spawn Tool</bold></gradient>";
    public static final String ARENA_TOOL_NAME = "<gradient:#00BFFF:#7CFC00><bold>Arena Tool</bold></gradient>";

    private final BlazesWildKits plugin;
    private final Map<String, ArenaDefinition> arenas = new LinkedHashMap<>();
    private final Map<UUID, ArenaConfigureSession> sessions = new ConcurrentHashMap<>();
    /** worldName:packedLong -> record */
    private final Map<String, PlacedBlockRecord> placedBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> protectedFall = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> wasOnPlatform = new ConcurrentHashMap<>();

    private boolean timedResetEnabled;
    private int timedResetMinutes = 5;
    private BukkitTask resetTask;
    private String activeArenaId;

    public ArenaManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        arenas.clear();
        File file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) {
            plugin.saveResource("arenas.yml", false);
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        timedResetEnabled = yaml.getBoolean("timed-reset.enabled", false);
        timedResetMinutes = Math.max(1, yaml.getInt("timed-reset.minutes", 5));
        activeArenaId = yaml.getString("active-arena");

        ConfigurationSection section = yaml.getConfigurationSection("arenas");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ArenaDefinition arena = ArenaDefinition.load(id, section.getConfigurationSection(id));
                if (arena != null) {
                    arenas.put(arena.getId(), arena);
                }
            }
        }

        // Backwards compatibility: seed a default arena from legacy lobby/drop if empty
        if (arenas.isEmpty()) {
            migrateFromLegacyRegions();
        }
        if (activeArenaId == null || !arenas.containsKey(activeArenaId)) {
            activeArenaId = arenas.isEmpty() ? null : arenas.keySet().iterator().next();
        }
        restartResetTask();
    }

    private void migrateFromLegacyRegions() {
        CuboidRegion lobby = plugin.getRegionManager().getLobbyRegion();
        CuboidRegion drop = plugin.getRegionManager().getDropRegion();
        Location spawn = plugin.getSpawnManager().getSpawn();
        if (drop == null && lobby == null) return;

        ArenaDefinition arena = new ArenaDefinition("default");
        if (spawn != null) {
            arena.setSpawnPlatform(spawn);
        } else if (lobby != null && lobby.getWorld() != null) {
            arena.setSpawnPlatform(new Location(
                    lobby.getWorld(),
                    (lobby.getMinX() + lobby.getMaxX()) / 2.0 + 0.5,
                    lobby.getMaxY(),
                    (lobby.getMinZ() + lobby.getMaxZ()) / 2.0 + 0.5
            ));
        }
        if (drop != null) {
            arena.setPlayableRegion(drop);
        }
        if (arena.getSpawnPlatform() != null || arena.getPlayableRegion() != null) {
            arenas.put(arena.getId(), arena);
            plugin.getLogger().info("Migrated legacy lobby/drop regions into arena 'default'.");
        }
    }

    public void save() {
        File file = new File(plugin.getDataFolder(), "arenas.yml");
        FileConfiguration yaml = new YamlConfiguration();
        yaml.set("timed-reset.enabled", timedResetEnabled);
        yaml.set("timed-reset.minutes", timedResetMinutes);
        yaml.set("active-arena", activeArenaId);
        ConfigurationSection section = yaml.createSection("arenas");
        for (ArenaDefinition arena : arenas.values()) {
            arena.save(section.createSection(arena.getId()));
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save arenas.yml: " + e.getMessage());
        }
    }

    public Collection<ArenaDefinition> getArenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }

    public Optional<ArenaDefinition> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(arenas.get(id.toLowerCase(Locale.ROOT)));
    }

    public ArenaDefinition getActiveArena() {
        if (activeArenaId == null) return null;
        return arenas.get(activeArenaId);
    }

    public void setActiveArena(String id) {
        if (id == null) return;
        String key = id.toLowerCase(Locale.ROOT);
        if (arenas.containsKey(key)) {
            activeArenaId = key;
            save();
        }
    }

    public ArenaConfigureSession startConfigure(Player player, String arenaId, boolean create) {
        String id = arenaId.toLowerCase(Locale.ROOT);
        if (create && arenas.containsKey(id)) {
            return null;
        }
        if (!create && !arenas.containsKey(id)) {
            return null;
        }
        ArenaConfigureSession session = new ArenaConfigureSession(player.getUniqueId(), id, create);
        if (!create) {
            ArenaDefinition existing = arenas.get(id);
            if (existing.getSpawnPlatform() != null) {
                session.setSpawnPlatform(existing.getSpawnPlatform());
            }
            CuboidRegion region = existing.getPlayableRegion();
            if (region != null && region.getWorld() != null) {
                session.setPos1(new Location(region.getWorld(), region.getMinX(), region.getMinY(), region.getMinZ()));
                session.setPos2(new Location(region.getWorld(), region.getMaxX(), region.getMaxY(), region.getMaxZ()));
            }
        }
        sessions.put(player.getUniqueId(), session);
        giveTools(player);
        return session;
    }

    public ArenaConfigureSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void clearSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public boolean saveSession(Player player) {
        ArenaConfigureSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.canSave()) return false;
        ArenaDefinition arena = arenas.getOrDefault(session.getArenaId(), new ArenaDefinition(session.getArenaId()));
        arena.setSpawnPlatform(session.getSpawnPlatform());
        arena.setPlayableRegion(new CuboidRegion(session.getPos1(), session.getPos2()));
        arenas.put(arena.getId(), arena);
        if (activeArenaId == null) {
            activeArenaId = arena.getId();
        }
        save();
        sessions.remove(player.getUniqueId());

        // Keep legacy drop/lobby in sync when configuring default arena
        if ("default".equals(arena.getId())) {
            if (arena.getPlayableRegion() != null) {
                plugin.getRegionManager().setDropRegion(arena.getPlayableRegion());
            }
            if (arena.getSpawnPlatform() != null) {
                // Approximate lobby as platform radius cuboid for legacy listeners
                Location center = arena.getSpawnPlatform();
                int r = (int) Math.ceil(arena.getPlatformRadius());
                int h = (int) Math.ceil(arena.getPlatformHeight());
                CuboidRegion lobby = new CuboidRegion(
                        center.getWorld().getName(),
                        center.getBlockX() - r, center.getBlockY() - h, center.getBlockZ() - r,
                        center.getBlockX() + r, center.getBlockY() + h, center.getBlockZ() + r
                );
                plugin.getRegionManager().setLobbyRegion(lobby);
            }
        }
        return true;
    }

    public void giveTools(Player player) {
        player.getInventory().addItem(createSpawnTool(), createArenaTool());
    }

    public ItemStack createSpawnTool() {
        return new ItemBuilder(Material.BLAZE_ROD)
                .name(SPAWN_TOOL_NAME)
                .lore(
                        "<gray>Right-click anywhere (including air)",
                        "<gray>to set the <yellow>SPAWN PLATFORM</yellow>",
                        "<dark_gray>Safe zone: no PvP, hunger, break, place,",
                        "<dark_gray>fire, explosions, drops, or fall damage"
                )
                .glow()
                .build();
    }

    public ItemStack createArenaTool() {
        return new ItemBuilder(Material.STICK)
                .name(ARENA_TOOL_NAME)
                .lore(
                        "<gray>Left-click: <aqua>Position 1</aqua>",
                        "<gray>Right-click: <aqua>Position 2</aqua>",
                        "<yellow>Defines the playable arena</yellow>",
                        "<dark_gray>Natural terrain protected;",
                        "<dark_gray>player blocks placeable/breakable"
                )
                .glow()
                .build();
    }

    public boolean isSpawnTool(ItemStack item) {
        return matchesTool(item, Material.BLAZE_ROD, "spawn tool");
    }

    public boolean isArenaTool(ItemStack item) {
        return matchesTool(item, Material.STICK, "arena tool");
    }

    private boolean matchesTool(ItemStack item, Material type, String plainNeedle) {
        if (item == null || item.getType() != type || !item.hasItemMeta()) return false;
        var meta = item.getItemMeta();
        if (meta == null || meta.displayName() == null) return false;
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(meta.displayName());
        return plain.toLowerCase(Locale.ROOT).contains(plainNeedle);
    }

    public ArenaDefinition findArenaAt(Location location) {
        if (location == null) return null;
        for (ArenaDefinition arena : arenas.values()) {
            if (arena.isOnSpawnPlatform(location) || arena.isInPlayable(location)) {
                return arena;
            }
        }
        return null;
    }

    public boolean isOnAnySpawnPlatform(Location location) {
        for (ArenaDefinition arena : arenas.values()) {
            if (arena.isOnSpawnPlatform(location)) return true;
        }
        // Legacy lobby fallback
        return plugin.getRegionManager().isInLobby(location);
    }

    public boolean isInAnyPlayable(Location location) {
        for (ArenaDefinition arena : arenas.values()) {
            if (arena.isInPlayable(location)) return true;
        }
        return plugin.getRegionManager().isInDrop(location);
    }

    public boolean isInProtectedSafeZone(Location location) {
        return isOnAnySpawnPlatform(location);
    }

    public void markPlayerPlaced(Block block) {
        if (block == null) return;
        Location loc = block.getLocation();
        if (!isInAnyPlayable(loc)) return;
        // Capture what was there before placement (air for empty, or replaced block)
        PlacedBlockRecord record = new PlacedBlockRecord(loc, Material.AIR.createBlockData());
        placedBlocks.put(PlacedBlockRecord.worldKey(loc.getWorld().getName(), record.key()), record);
        // Also mark in legacy region manager for compatibility
        plugin.getRegionManager().markPlayerPlaced(loc);
    }

    public void markPlayerPlacedReplacing(Block block, org.bukkit.block.data.BlockData replaced) {
        if (block == null) return;
        Location loc = block.getLocation();
        if (!isInAnyPlayable(loc)) return;
        PlacedBlockRecord record = new PlacedBlockRecord(loc, replaced == null ? Material.AIR.createBlockData() : replaced);
        placedBlocks.put(PlacedBlockRecord.worldKey(loc.getWorld().getName(), record.key()), record);
        plugin.getRegionManager().markPlayerPlaced(loc);
    }

    public boolean isPlayerPlaced(Location location) {
        if (location == null || location.getWorld() == null) return false;
        String key = PlacedBlockRecord.worldKey(location.getWorld().getName(),
                PlacedBlockRecord.pack(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        if (placedBlocks.containsKey(key)) return true;
        return plugin.getRegionManager().isPlayerPlaced(location);
    }

    public void unmarkPlayerPlaced(Location location) {
        if (location == null || location.getWorld() == null) return;
        String key = PlacedBlockRecord.worldKey(location.getWorld().getName(),
                PlacedBlockRecord.pack(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        placedBlocks.remove(key);
        plugin.getRegionManager().unmarkPlayerPlaced(location);
    }

    public int resetAllPlayerBlocks() {
        List<PlacedBlockRecord> snapshot = new ArrayList<>(placedBlocks.values());
        placedBlocks.clear();
        int restored = 0;
        // Batch restore — no physics to avoid lag spikes
        for (PlacedBlockRecord record : snapshot) {
            if (record.restore()) {
                restored++;
                Location loc = record.toLocation();
                if (loc != null) {
                    plugin.getRegionManager().unmarkPlayerPlaced(loc);
                }
            }
        }
        return restored;
    }

    public void setTimedReset(boolean enabled, int minutes) {
        this.timedResetEnabled = enabled;
        if (minutes > 0) this.timedResetMinutes = minutes;
        save();
        restartResetTask();
    }

    public boolean isTimedResetEnabled() {
        return timedResetEnabled;
    }

    public int getTimedResetMinutes() {
        return timedResetMinutes;
    }

    private void restartResetTask() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }
        if (!timedResetEnabled) return;
        long period = timedResetMinutes * 60L * 20L;
        resetTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int count = resetAllPlayerBlocks();
            if (count > 0) {
                plugin.getLogger().info("Arena block reset restored " + count + " blocks.");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.getMessageService().send(player, "arena-blocks-reset", Map.of(
                            "count", String.valueOf(count)
                    ));
                }
            }
        }, period, period);
    }

    public void armProtectedFall(Player player) {
        protectedFall.put(player.getUniqueId(), Boolean.TRUE);
    }

    public boolean consumeProtectedFall(Player player) {
        return Boolean.TRUE.equals(protectedFall.remove(player.getUniqueId()));
    }

    public void clearProtectedFall(Player player) {
        protectedFall.remove(player.getUniqueId());
        wasOnPlatform.remove(player.getUniqueId());
    }

    public Map<UUID, Boolean> getWasOnPlatform() {
        return wasOnPlatform;
    }

    public int getTrackedBlockCount() {
        return placedBlocks.size();
    }

    public void shutdown() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }
        save();
        // Do not leave permanent player blocks if timed reset was on — optional soft clear skipped for safety
        sessions.clear();
        protectedFall.clear();
        wasOnPlatform.clear();
    }
}
