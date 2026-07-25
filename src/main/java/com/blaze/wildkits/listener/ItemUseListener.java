package com.blaze.wildkits.listener;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class ItemUseListener implements Listener {

    private final BlazesWildKits plugin;

    public ItemUseListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Material type = event.getItem().getType();
        if (type == Material.GOLDEN_APPLE || type == Material.ENCHANTED_GOLDEN_APPLE) {
            plugin.getQuestManager().progress(event.getPlayer(), "golden_apple", 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPearl(ProjectileLaunchEvent event) {
        if (event.getEntity().getType().name().contains("ENDER_PEARL")) {
            ProjectileSource source = event.getEntity().getShooter();
            if (source instanceof Player player) {
                plugin.getQuestManager().progress(player, "ender_pearl", 1);
            }
        }
    }
}
