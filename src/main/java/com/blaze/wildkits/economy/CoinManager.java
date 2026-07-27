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
                long before = data.getCoins();
                data.addCoins(reward);
                int gained = (int) Math.max(0, data.getCoins() - before);
                plugin.getQuestManager().progress(player, "collect_coins", Math.max(reward, gained));
                plugin.getMessageService().send(player, "playtime-reward", Map.of("coins", String.valueOf(gained)));
                plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
            }
        }, period, period);
    }

    public void rewardKill(Player killer, int streak) {
        int base = plugin.getConfigManager().getCoinsPerKill();
        int bonus = plugin.getConfigManager().getCoinsPerKillstreakBonus() * Math.max(0, streak - 1);
        int total = base + bonus;
        PlayerData data = plugin.getPlayerDataManager().get(killer);
        long before = data.getCoins();
        data.addCoins(total);
        int gained = (int) Math.max(0, data.getCoins() - before);
        if (plugin.getVaultHook() != null && plugin.getVaultHook().isEnabled()) {
            plugin.getVaultHook().deposit(killer, gained);
        }
        plugin.getQuestManager().progress(killer, "collect_coins", gained);
        plugin.getMessageService().send(killer, "coins-kill", Map.of("coins", String.valueOf(gained)));
    }

    public boolean claimDaily(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        long now = System.currentTimeMillis();
        long since = now - data.getLastDaily();
        if (since < TimeUnit.DAYS.toMillis(1) && data.getLastDaily() > 0) {
            long remaining = TimeUnit.DAYS.toMillis(1) - since;
            long hours = TimeUnit.MILLISECONDS.toHours(remaining) + 1;
            plugin.getMessageService().send(player, "daily-wait", Map.of("hours", String.valueOf(hours)));
            return false;
        }

        // Login streak: if claimed within 48h of last claim, continue; else reset
        if (data.getLastDaily() > 0 && since <= TimeUnit.DAYS.toMillis(2)) {
            data.setLoginStreak(data.getLoginStreak() + 1);
        } else {
            data.setLoginStreak(1);
        }

        int reward = plugin.getConfigManager().getDailyRewardCoins();
        int streakBonus = plugin.getConfigManager().getConfig().getInt("economy.login-streak-bonus", 15)
                * Math.max(0, data.getLoginStreak() - 1);
        int total = reward + streakBonus;
        data.setLastDaily(now);
        long before = data.getCoins();
        data.addCoins(total);
        int gained = (int) Math.max(0, data.getCoins() - before);
        // Login reward key gift every 7 days
        if (data.getLoginStreak() > 0 && data.getLoginStreak() % 7 == 0) {
            data.addCrateKeys("common", 1);
            data.addKitRerolls(1);
        }
        plugin.getQuestManager().progress(player, "collect_coins", gained);
        plugin.getMessageService().send(player, "daily-claim", Map.of(
                "coins", String.valueOf(gained),
                "streak", String.valueOf(data.getLoginStreak())
        ));
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
