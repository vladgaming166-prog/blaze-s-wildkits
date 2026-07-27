package com.blaze.wildkits.listener;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Quest progress only — no world protection (use WorldGuard).
 */
public final class QuestProgressListener implements Listener {

    private final BlazesWildKits plugin;

    public QuestProgressListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        plugin.getQuestManager().progress(event.getPlayer(), "place_block", 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        plugin.getQuestManager().progress(event.getPlayer(), "break_placed", 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (event.getDamager() instanceof Player p) attacker = p;
        else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj
                && proj.getShooter() instanceof Player p) attacker = p;
        if (attacker == null) return;
        int dmg = (int) Math.max(1, event.getFinalDamage());
        plugin.getQuestManager().progress(attacker, "deal_damage", dmg);
        plugin.getQuestManager().progress(victim, "take_damage", dmg);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Reserved for future match-tracking; no region logic
    }
}
