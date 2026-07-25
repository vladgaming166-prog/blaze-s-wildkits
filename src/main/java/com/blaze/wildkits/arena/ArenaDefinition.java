package com.blaze.wildkits.arena;

import com.blaze.wildkits.region.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A named WildKits arena with a safe spawn platform and playable combat region.
 */
public final class ArenaDefinition {

    private final String id;
    private Location spawnPlatform;
    private CuboidRegion playableRegion;
    private double platformRadius;
    private double platformHeight;

    public ArenaDefinition(String id) {
        this.id = id == null ? "default" : id.toLowerCase();
        this.platformRadius = 6.0;
        this.platformHeight = 4.0;
    }

    public String getId() {
        return id;
    }

    public Location getSpawnPlatform() {
        return spawnPlatform == null ? null : spawnPlatform.clone();
    }

    public void setSpawnPlatform(Location spawnPlatform) {
        this.spawnPlatform = spawnPlatform == null ? null : spawnPlatform.clone();
    }

    public CuboidRegion getPlayableRegion() {
        return playableRegion;
    }

    public void setPlayableRegion(CuboidRegion playableRegion) {
        this.playableRegion = playableRegion;
    }

    public double getPlatformRadius() {
        return platformRadius;
    }

    public void setPlatformRadius(double platformRadius) {
        this.platformRadius = Math.max(1.0, platformRadius);
    }

    public double getPlatformHeight() {
        return platformHeight;
    }

    public void setPlatformHeight(double platformHeight) {
        this.platformHeight = Math.max(1.0, platformHeight);
    }

    public boolean isConfigured() {
        return spawnPlatform != null && playableRegion != null;
    }

    public boolean isOnSpawnPlatform(Location location) {
        if (spawnPlatform == null || location == null || location.getWorld() == null) return false;
        if (spawnPlatform.getWorld() == null) return false;
        if (!spawnPlatform.getWorld().equals(location.getWorld())) return false;
        double dx = location.getX() - spawnPlatform.getX();
        double dz = location.getZ() - spawnPlatform.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double dy = Math.abs(location.getY() - spawnPlatform.getY());
        return horizontal <= platformRadius && dy <= platformHeight;
    }

    public boolean isInPlayable(Location location) {
        return playableRegion != null && playableRegion.contains(location);
    }

    public void save(ConfigurationSection section) {
        section.set("id", id);
        section.set("platform-radius", platformRadius);
        section.set("platform-height", platformHeight);
        if (spawnPlatform != null && spawnPlatform.getWorld() != null) {
            section.set("spawn.world", spawnPlatform.getWorld().getName());
            section.set("spawn.x", spawnPlatform.getX());
            section.set("spawn.y", spawnPlatform.getY());
            section.set("spawn.z", spawnPlatform.getZ());
            section.set("spawn.yaw", spawnPlatform.getYaw());
            section.set("spawn.pitch", spawnPlatform.getPitch());
        }
        if (playableRegion != null) {
            playableRegion.save(section.createSection("playable"));
        }
    }

    public static ArenaDefinition load(String id, ConfigurationSection section) {
        if (section == null) return null;
        ArenaDefinition arena = new ArenaDefinition(id);
        arena.setPlatformRadius(section.getDouble("platform-radius", 6.0));
        arena.setPlatformHeight(section.getDouble("platform-height", 4.0));
        ConfigurationSection spawn = section.getConfigurationSection("spawn");
        if (spawn != null && spawn.getString("world") != null) {
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(spawn.getString("world"));
            if (world != null) {
                arena.setSpawnPlatform(new Location(
                        world,
                        spawn.getDouble("x"),
                        spawn.getDouble("y"),
                        spawn.getDouble("z"),
                        (float) spawn.getDouble("yaw"),
                        (float) spawn.getDouble("pitch")
                ));
            }
        }
        arena.setPlayableRegion(CuboidRegion.load(section.getConfigurationSection("playable")));
        return arena;
    }
}
