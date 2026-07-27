package com.blaze.wildkits.crate;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class CrateListener implements Listener {

    private final BlazesWildKits plugin;

    public CrateListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        plugin.getCrateManager().byLocation(event.getClickedBlock().getLocation()).ifPresent(crate -> {
            event.setCancelled(true);
            plugin.getCrateManager().tryOpen(event.getPlayer(), crate);
        });
    }
}
