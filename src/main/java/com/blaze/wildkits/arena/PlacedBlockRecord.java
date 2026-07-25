package com.blaze.wildkits.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

/**
 * Tracks a player-placed block and the original block data beneath it for efficient restore.
 */
public final class PlacedBlockRecord {

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private final String originalData;
    private final long placedAt;

    public PlacedBlockRecord(Location location, BlockData original) {
        this.worldName = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.originalData = original == null ? Material.AIR.createBlockData().getAsString() : original.getAsString();
        this.placedAt = System.currentTimeMillis();
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public long getPlacedAt() {
        return placedAt;
    }

    public long key() {
        return pack(x, y, z);
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z);
    }

    public boolean restore() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;
        try {
            BlockData data = Bukkit.createBlockData(originalData);
            world.getBlockAt(x, y, z).setBlockData(data, false);
            return true;
        } catch (IllegalArgumentException ex) {
            world.getBlockAt(x, y, z).setType(Material.AIR, false);
            return true;
        }
    }

    public static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    public static String worldKey(String world, long packed) {
        return world + ':' + packed;
    }
}
