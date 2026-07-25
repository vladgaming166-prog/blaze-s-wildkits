package com.blaze.wildkits.setup;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.region.SelectionSession;
import com.blaze.wildkits.util.ItemBuilder;
import com.blaze.wildkits.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SetupGui {

    private final BlazesWildKits plugin;
    private final Map<UUID, Map<Integer, String>> actions = new HashMap<>();

    public SetupGui(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 45, TextUtil.parse("<gradient:#FF4500:#FFD700><bold>WildKits Setup</bold></gradient>"));
        Map<Integer, String> map = new HashMap<>();

        boolean lobby = plugin.getRegionManager().hasLobby();
        boolean drop = plugin.getRegionManager().hasDrop();
        boolean spawn = plugin.getSpawnManager().getSpawn() != null;

        set(inv, map, 10, new ItemBuilder(Material.GRASS_BLOCK)
                .name("<green>Lobby Region")
                .lore(lobby ? "<green>Configured" : "<red>Not set",
                        "<gray>Safe spawn protections",
                        "<yellow>Click: take wand + select lobby")
                .build(), "setup:lobby");

        set(inv, map, 12, new ItemBuilder(Material.RED_CONCRETE)
                .name("<red>Drop Region")
                .lore(drop ? "<green>Configured" : "<red>Not set",
                        "<gray>PvP arena rules",
                        "<yellow>Click: take wand + select drop")
                .build(), "setup:drop");

        set(inv, map, 14, new ItemBuilder(Material.ENDER_PEARL)
                .name("<aqua>Spawn")
                .lore(spawn ? "<green>Configured" : "<red>Not set",
                        "<gray>Global lobby spawn",
                        "<yellow>Click: set spawn here")
                .build(), "setup:spawn");

        set(inv, map, 16, new ItemBuilder(Material.GOLDEN_AXE)
                .name("<gold>Region Wand")
                .lore("<gray>WorldEdit-like selector",
                        "<yellow>Click: give wand")
                .build(), "setup:wand");

        set(inv, map, 28, new ItemBuilder(Material.CHEST)
                .name("<yellow>Crates")
                .lore("<gray>/wk crate create <id> <rarity>",
                        "<gray>Then stand on block and",
                        "<yellow>/wk crate set <id>")
                .build(), "setup:crates");

        set(inv, map, 30, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
                .name("<light_purple>Quest NPC")
                .lore("<gray>Creates Citizens NPC",
                        "<yellow>Opens quest menu on click")
                .build(), "setup:quest_npc");

        set(inv, map, 32, new ItemBuilder(Material.EMERALD)
                .name("<green>Shop NPC")
                .lore("<gray>Creates Citizens NPC",
                        "<yellow>Opens shop on click")
                .build(), "setup:shop_npc");

        set(inv, map, 34, new ItemBuilder(Material.LIME_CONCRETE)
                .name("<green><bold>Finish Setup</bold>")
                .lore(lobby && drop && spawn ? "<green>Ready to finish" : "<red>Complete lobby, drop, spawn first")
                .build(), "setup:finish");

        actions.put(player.getUniqueId(), map);
        player.openInventory(inv);
    }

    private void set(Inventory inv, Map<Integer, String> map, int slot, org.bukkit.inventory.ItemStack item, String action) {
        inv.setItem(slot, item);
        map.put(slot, action);
    }

    public boolean handle(Player player, int slot) {
        Map<Integer, String> map = actions.get(player.getUniqueId());
        if (map == null) return false;
        String action = map.get(slot);
        if (action == null) return false;
        switch (action) {
            case "setup:lobby" -> {
                player.closeInventory();
                plugin.getRegionManager().giveWand(player, SelectionSession.Target.LOBBY);
            }
            case "setup:drop" -> {
                player.closeInventory();
                plugin.getRegionManager().giveWand(player, SelectionSession.Target.DROP);
            }
            case "setup:spawn" -> {
                plugin.getSpawnManager().setSpawn(player.getLocation());
                plugin.getMessageService().send(player, "spawn-set");
                open(player);
            }
            case "setup:wand" -> {
                player.closeInventory();
                plugin.getRegionManager().giveWand(player, SelectionSession.Target.NONE);
            }
            case "setup:crates" -> {
                player.closeInventory();
                plugin.getMessageService().send(player, "setup-crates-help");
            }
            case "setup:quest_npc" -> {
                player.closeInventory();
                if (plugin.getCitizensHook() != null && plugin.getCitizensHook().isEnabled()) {
                    plugin.getCitizensHook().createNpc(player, "WildKits Quests");
                } else {
                    plugin.getMessageService().send(player, "citizens-missing");
                }
            }
            case "setup:shop_npc" -> {
                player.closeInventory();
                if (plugin.getCitizensHook() != null && plugin.getCitizensHook().isEnabled()) {
                    plugin.getCitizensHook().createNpc(player, "WildKits Shop");
                } else {
                    plugin.getMessageService().send(player, "citizens-missing");
                }
            }
            case "setup:finish" -> {
                boolean ok = plugin.getRegionManager().hasLobby()
                        && plugin.getRegionManager().hasDrop()
                        && plugin.getSpawnManager().getSpawn() != null;
                if (!ok) {
                    plugin.getMessageService().send(player, "setup-incomplete");
                    open(player);
                    return true;
                }
                plugin.getRegionManager().setSetupComplete(true);
                plugin.getMessageService().send(player, "setup-finished");
                player.closeInventory();
            }
            default -> {
            }
        }
        return true;
    }

    public void clear(Player player) {
        actions.remove(player.getUniqueId());
    }

    public boolean isSetupInventory(String title) {
        return title != null && title.toLowerCase().contains("setup");
    }
}
