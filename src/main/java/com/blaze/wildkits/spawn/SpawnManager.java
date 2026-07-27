package com.blaze.wildkits.spawn;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class SpawnManager {

    private final BlazesWildKits plugin;
    private Location spawn;

    public SpawnManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (!config.isConfigurationSection("spawn") || config.getString("spawn.world") == null) {
            this.spawn = null;
            return;
        }
        World world = Bukkit.getWorld(config.getString("spawn.world", "world"));
        if (world == null) {
            this.spawn = null;
            return;
        }
        this.spawn = new Location(
                world,
                config.getDouble("spawn.x"),
                config.getDouble("spawn.y"),
                config.getDouble("spawn.z"),
                (float) config.getDouble("spawn.yaw"),
                (float) config.getDouble("spawn.pitch")
        );
    }

    public void setSpawn(Location location) {
        this.spawn = location.clone();
        FileConfiguration config = plugin.getConfigManager().getConfig();
        config.set("spawn.world", location.getWorld().getName());
        config.set("spawn.x", location.getX());
        config.set("spawn.y", location.getY());
        config.set("spawn.z", location.getZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location getSpawn() {
        return spawn == null ? null : spawn.clone();
    }

    public boolean teleport(Player player) {
        if (spawn == null || spawn.getWorld() == null) {
            return false;
        }
        player.teleport(spawn);
        return true;
    }
}
