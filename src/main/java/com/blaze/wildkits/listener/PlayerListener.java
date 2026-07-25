package com.blaze.wildkits.listener;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;

public final class PlayerListener implements Listener {

    private final BlazesWildKits plugin;

    public PlayerListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().loadAsync(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            prepareLife(player, true);
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getScoreboardManager().hide(player);
        plugin.getMenuService().clear(player);
        plugin.getPlayerDataManager().unload(player.getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfigManager().isRespawnTeleport() && plugin.getSpawnManager().getSpawn() != null) {
            event.setRespawnLocation(plugin.getSpawnManager().getSpawn());
        }
        int delay = plugin.getConfigManager().getRespawnDelaySeconds();
        long ticks = Math.max(1, delay) * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            prepareLife(player, false);
        }, ticks);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data.isProtectedSpawn()) {
            event.setCancelled(true);
        }
    }

    private void prepareLife(Player player, boolean join) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        if (plugin.getConfigManager().isRespawnHeal()) {
            var max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            player.setHealth(max != null ? max.getValue() : 20.0);
        }
        if (plugin.getConfigManager().isRespawnFeed()) {
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
        if (plugin.getConfigManager().isRespawnClearEffects()) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
        if (plugin.getConfigManager().isRespawnTeleport()) {
            plugin.getSpawnManager().teleport(player);
        }

        int protection = plugin.getConfigManager().getRespawnProtectionSeconds();
        if (protection > 0) {
            plugin.getPlayerDataManager().get(player).setProtection(protection * 1000L);
        }

        if (plugin.getConfigManager().isAutoRandomKits()) {
            plugin.getKitManager().giveRandomKit(player);
        } else if (join) {
            plugin.getMessageService().send(player, "choose-kit");
            plugin.getMenuService().openMain(player);
        }
    }
}
