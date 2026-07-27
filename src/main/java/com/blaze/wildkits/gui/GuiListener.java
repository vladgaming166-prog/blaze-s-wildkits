package com.blaze.wildkits.gui;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class GuiListener implements Listener {

    private final BlazesWildKits plugin;

    public GuiListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GuiSession session = plugin.getMenuService().session(player);
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        if (!isWildKitsMenu(title) && session.getAction(event.getRawSlot()) == null) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null) return;

        String action = session.getAction(event.getRawSlot());
        if (action == null) return;
        boolean right = event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT;
        boolean shift = event.getClick().isShiftClick();
        plugin.getMenuService().handleAction(player, action, right, shift);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        if (isWildKitsMenu(title)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            plugin.getMenuService().session(player).clearActions();
        }
    }

    private boolean isWildKitsMenu(String title) {
        if (title == null) return false;
        String t = title.toLowerCase();
        return t.contains("wildkits") || t.contains("preview") || t.contains("shop")
                || t.contains("kits") || t.contains("particle") || t.contains("categories")
                || t.contains("favorites") || t.contains("recent") || t.contains("search")
                || t.contains("quest") || t.contains("crate") || t.contains("cosmetics")
                || t.contains("boosters") || t.contains("reroll");
    }
}
