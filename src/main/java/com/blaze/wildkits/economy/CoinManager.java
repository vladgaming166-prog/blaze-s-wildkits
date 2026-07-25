package com.blaze.wildkits.economy;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class CoinManager {

    private final BlazesWildKits plugin;
    private BukkitTask playtimeTask;

    public CoinManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void startPlaytimeTask() {
        shutdown();
        int minutes = Math.max(1, plugin.getConfigManager().getPlaytimeIntervalMinutes());
        long period = minutes * 60L * 20L;
        playtimeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int reward = plugin.getConfigManager().getPlaytimeCoins();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = plugin.getPlayerDataManager().get(player);
                data.addPlaytimeSeconds(minutes * 60L);
                data.addCoins(reward);
                plugin.getMessageService().send(player, "playtime-reward", Map.of("coins", String.valueOf(reward)));
                plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
            }
        }, period, period);
    }

    public void rewardKill(Player killer, int streak) {
        int base = plugin.getConfigManager().getCoinsPerKill();
        int bonus = plugin.getConfigManager().getCoinsPerKillstreakBonus() * Math.max(0, streak - 1);
        int total = base + bonus;
        PlayerData data = plugin.getPlayerDataManager().get(killer);
        data.addCoins(total);
        if (plugin.getVaultHook() != null && plugin.getVaultHook().isEnabled()) {
            plugin.getVaultHook().deposit(killer, total);
        }
        plugin.getMessageService().send(killer, "coins-kill", Map.of("coins", String.valueOf(total)));
    }

    public boolean claimDaily(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        long now = System.currentTimeMillis();
        if (now - data.getLastDaily() < TimeUnit.DAYS.toMillis(1)) {
            long remaining = TimeUnit.DAYS.toMillis(1) - (now - data.getLastDaily());
            long hours = TimeUnit.MILLISECONDS.toHours(remaining) + 1;
            plugin.getMessageService().send(player, "daily-wait", Map.of("hours", String.valueOf(hours)));
            return false;
        }
        int reward = plugin.getConfigManager().getDailyRewardCoins();
        data.setLastDaily(now);
        data.addCoins(reward);
        plugin.getMessageService().send(player, "daily-claim", Map.of("coins", String.valueOf(reward)));
        plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
        return true;
    }

    public void shutdown() {
        if (playtimeTask != null) {
            playtimeTask.cancel();
            playtimeTask = null;
        }
    }
}
