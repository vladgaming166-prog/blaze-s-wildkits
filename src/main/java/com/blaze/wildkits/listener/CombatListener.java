package com.blaze.wildkits.listener;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

public final class CombatListener implements Listener {

    private final BlazesWildKits plugin;

    public CombatListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        PlayerData victimData = plugin.getPlayerDataManager().get(victim);
        victimData.addDeath();
        plugin.getParticleManager().playDeathEffect(victim);
        plugin.getPlayerDataManager().saveAsync(victim.getUniqueId());

        Player killer = victim.getKiller();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            PlayerData killerData = plugin.getPlayerDataManager().get(killer);
            killerData.addKill();
            int streak = killerData.getKillstreak();
            int base = plugin.getConfigManager().getCoinsPerKill();
            int bonus = plugin.getConfigManager().getCoinsPerKillstreakBonus() * Math.max(0, streak - 1);
            plugin.getCoinManager().rewardKill(killer, streak);
            plugin.getParticleManager().playVictoryEffect(killer);
            plugin.getMessageService().send(killer, "player-kill", Map.of(
                    "victim", victim.getName(),
                    "streak", String.valueOf(streak)
            ));
            plugin.getDatabaseManager().logKillAsync(
                    killer.getUniqueId().toString(),
                    victim.getUniqueId().toString(),
                    killerData.getCurrentKit(),
                    base + bonus
            );
            plugin.getPlayerDataManager().saveAsync(killer.getUniqueId());
            plugin.getScoreboardManager().update(killer);
        }
        plugin.getScoreboardManager().update(victim);

        // Keep inventory empty on death for KitPvP flow
        event.getDrops().clear();
        event.setDroppedExp(0);
    }
}
