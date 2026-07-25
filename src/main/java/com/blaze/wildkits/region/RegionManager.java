package com.blaze.wildkits.region;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.util.ItemBuilder;
import com.blaze.wildkits.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionManager {

    public static final String WAND_NAME = "<gradient:#FF4500:#FFD700><bold>WildKits Wand</bold></gradient>";

    private final BlazesWildKits plugin;
    private final Map<UUID, SelectionSession> selections = new ConcurrentHashMap<>();
    private final Set<String> playerPlacedBlocks = ConcurrentHashMap.newKeySet();
    private CuboidRegion lobbyRegion;
    private CuboidRegion dropRegion;
    private boolean setupComplete;

    public RegionManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "regions.yml");
        if (!file.exists()) {
            plugin.saveResource("regions.yml", false);
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        lobbyRegion = CuboidRegion.load(yaml.getConfigurationSection("lobby"));
        dropRegion = CuboidRegion.load(yaml.getConfigurationSection("drop"));
        setupComplete = yaml.getBoolean("setup-complete", lobbyRegion != null && dropRegion != null
                && plugin.getSpawnManager().getSpawn() != null);
    }

    public void save() {
        File file = new File(plugin.getDataFolder(), "regions.yml");
        FileConfiguration yaml = new YamlConfiguration();
        if (lobbyRegion != null) {
            lobbyRegion.save(yaml.createSection("lobby"));
        }
        if (dropRegion != null) {
            dropRegion.save(yaml.createSection("drop"));
        }
        yaml.set("setup-complete", setupComplete);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save regions.yml: " + e.getMessage());
        }
    }

    public SelectionSession session(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), SelectionSession::new);
    }

    public void clearSelection(Player player) {
        selections.remove(player.getUniqueId());
    }

    public ItemStack createWand() {
        return new ItemBuilder(Material.GOLDEN_AXE)
                .name(WAND_NAME)
                .lore(
                        "<gray>Left-click: set Pos1",
                        "<gray>Right-click: set Pos2",
                        "<yellow>/wk wand save</yellow> <gray>to save region"
                )
                .glow()
                .build();
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_AXE || !item.hasItemMeta()) return false;
        var meta = item.getItemMeta();
        if (meta == null || meta.displayName() == null) return false;
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(meta.displayName());
        return plain.toLowerCase().contains("wildkits wand");
    }

    public void giveWand(Player player, SelectionSession.Target target) {
        SelectionSession session = session(player);
        session.setTarget(target);
        player.getInventory().addItem(createWand());
        plugin.getMessageService().send(player, "wand-given", Map.of(
                "target", target == SelectionSession.Target.NONE ? "region" : target.name().toLowerCase()
        ));
    }

    public boolean saveSelection(Player player) {
        SelectionSession session = session(player);
        if (!session.isComplete()) {
            plugin.getMessageService().send(player, "wand-incomplete");
            return false;
        }
        CuboidRegion region = session.toRegion();
        if (session.getTarget() == SelectionSession.Target.LOBBY) {
            lobbyRegion = region;
            plugin.getMessageService().send(player, "region-lobby-saved", Map.of("region", region.toString()));
        } else if (session.getTarget() == SelectionSession.Target.DROP) {
            dropRegion = region;
            plugin.getMessageService().send(player, "region-drop-saved", Map.of("region", region.toString()));
        } else {
            plugin.getMessageService().send(player, "wand-no-target");
            return false;
        }
        save();
        return true;
    }

    public boolean isInLobby(Location location) {
        return lobbyRegion != null && lobbyRegion.contains(location);
    }

    public boolean isInDrop(Location location) {
        return dropRegion != null && dropRegion.contains(location);
    }

    public boolean isInLobby(Player player) {
        return isInLobby(player.getLocation());
    }

    public boolean isInDrop(Player player) {
        return isInDrop(player.getLocation());
    }

    public void markPlayerPlaced(Location location) {
        if (location == null || location.getWorld() == null) return;
        playerPlacedBlocks.add(key(location));
    }

    public void unmarkPlayerPlaced(Location location) {
        if (location == null || location.getWorld() == null) return;
        playerPlacedBlocks.remove(key(location));
    }

    public boolean isPlayerPlaced(Location location) {
        return location != null && playerPlacedBlocks.contains(key(location));
    }

    private String key(Location location) {
        return location.getWorld().getName() + ':' + location.getBlockX() + ':'
                + location.getBlockY() + ':' + location.getBlockZ();
    }

    public CuboidRegion getLobbyRegion() { return lobbyRegion; }
    public CuboidRegion getDropRegion() { return dropRegion; }
    public void setLobbyRegion(CuboidRegion lobbyRegion) { this.lobbyRegion = lobbyRegion; save(); }
    public void setDropRegion(CuboidRegion dropRegion) { this.dropRegion = dropRegion; save(); }

    public boolean isSetupComplete() { return setupComplete; }
    public void setSetupComplete(boolean setupComplete) {
        this.setupComplete = setupComplete;
        save();
    }

    public boolean hasLobby() { return lobbyRegion != null; }
    public boolean hasDrop() { return dropRegion != null; }
}
