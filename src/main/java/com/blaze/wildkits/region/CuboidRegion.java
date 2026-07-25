package com.blaze.wildkits.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public final class CuboidRegion {

    private final String worldName;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public CuboidRegion(Location a, Location b) {
        if (a.getWorld() == null || b.getWorld() == null || !a.getWorld().equals(b.getWorld())) {
            throw new IllegalArgumentException("Both positions must be in the same world");
        }
        this.worldName = a.getWorld().getName();
        this.minX = Math.min(a.getBlockX(), b.getBlockX());
        this.minY = Math.min(a.getBlockY(), b.getBlockY());
        this.minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        this.maxX = Math.max(a.getBlockX(), b.getBlockX());
        this.maxY = Math.max(a.getBlockY(), b.getBlockY());
        this.maxZ = Math.max(a.getBlockZ(), b.getBlockZ());
    }

    public CuboidRegion(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = worldName;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public String getWorldName() { return worldName; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    public void save(ConfigurationSection section) {
        section.set("world", worldName);
        section.set("min-x", minX);
        section.set("min-y", minY);
        section.set("min-z", minZ);
        section.set("max-x", maxX);
        section.set("max-y", maxY);
        section.set("max-z", maxZ);
    }

    public static CuboidRegion load(ConfigurationSection section) {
        if (section == null || section.getString("world") == null) return null;
        return new CuboidRegion(
                section.getString("world"),
                section.getInt("min-x"),
                section.getInt("min-y"),
                section.getInt("min-z"),
                section.getInt("max-x"),
                section.getInt("max-y"),
                section.getInt("max-z")
        );
    }

    @Override
    public String toString() {
        return worldName + " (" + minX + "," + minY + "," + minZ + ") -> ("
                + maxX + "," + maxY + "," + maxZ + ")";
    }
}
