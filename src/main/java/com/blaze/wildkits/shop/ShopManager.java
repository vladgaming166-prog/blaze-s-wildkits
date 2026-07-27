package com.blaze.wildkits.shop;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.kit.KitDefinition;
import com.blaze.wildkits.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ShopManager {

    private final BlazesWildKits plugin;
    private final Map<String, ShopItem> items = new LinkedHashMap<>();

    public ShopManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        items.clear();
        ConfigurationSection section = plugin.getConfigManager().getShop().getConfigurationSection("items");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection item = section.getConfigurationSection(id);
                if (item == null) continue;
                ShopItem.Type type = ShopItem.Type.valueOf(item.getString("type", "TRAIL").toUpperCase(Locale.ROOT));
                Material icon = Material.matchMaterial(item.getString("icon", "EMERALD"));
                items.put(id.toLowerCase(Locale.ROOT), new ShopItem(
                        id.toLowerCase(Locale.ROOT),
                        item.getString("display-name", id),
                        item.getString("description", ""),
                        type,
                        icon,
                        item.getInt("price", 100),
                        item.getString("unlock-id", id),
                        item.getString("category", "Cosmetics")
                ));
            }
        }

        // Auto-add locked kits into shop
        for (KitDefinition kit : plugin.getKitManager().getKits()) {
            if (kit.isUnlockedByDefault() || kit.getPrice() <= 0) continue;
            String id = "kit_" + kit.getId();
            items.putIfAbsent(id, new ShopItem(
                    id,
                    kit.getDisplayName(),
                    kit.getDescription(),
                    ShopItem.Type.KIT,
                    kit.getIcon(),
                    kit.getPrice(),
                    kit.getId(),
                    "Kits"
            ));
        }
    }

    public Collection<ShopItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public List<ShopItem> getByCategory(String category) {
        List<ShopItem> list = new ArrayList<>();
        for (ShopItem item : items.values()) {
            if (item.getCategory().equalsIgnoreCase(category)) {
                list.add(item);
            }
        }
        return list;
    }

    public Optional<ShopItem> get(String id) {
        return Optional.ofNullable(items.get(id.toLowerCase(Locale.ROOT)));
    }

    public boolean purchase(Player player, ShopItem item) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (owns(data, item)) {
            plugin.getMessageService().send(player, "shop-owned");
            return false;
        }
        if (!data.takeCoins(item.getPrice())) {
            plugin.getMessageService().send(player, "shop-not-enough", Map.of("price", String.valueOf(item.getPrice())));
            return false;
        }
        applyUnlock(data, item);
        plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
        plugin.getMessageService().send(player, "shop-purchased", Map.of(
                "item", item.getDisplayName(),
                "price", String.valueOf(item.getPrice())
        ));
        return true;
    }

    public boolean owns(PlayerData data, ShopItem item) {
        return switch (item.getType()) {
            case KIT, KIT_UPGRADE -> data.hasUnlockedKit(item.getUnlockId());
            case BOOSTER, KIT_REROLL, CRATE_KEY -> false; // consumables — always repurchaseable
            default -> data.hasCosmetic(item.getUnlockId());
        };
    }

    private void applyUnlock(PlayerData data, ShopItem item) {
        switch (item.getType()) {
            case KIT, KIT_UPGRADE -> data.unlockKit(item.getUnlockId());
            case TRAIL -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveTrail(item.getUnlockId());
            }
            case DEATH_EFFECT -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveDeathEffect(item.getUnlockId());
            }
            case VICTORY_EFFECT -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveVictoryEffect(item.getUnlockId());
            }
            case KILL_EFFECT -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveKillEffect(item.getUnlockId());
            }
            case TAG -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveTag(item.getUnlockId());
            }
            case TITLE -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveTitle(item.getUnlockId());
                if (item.getUnlockId() != null && item.getUnlockId().startsWith("prefix_")) {
                    data.setActivePrefix(item.getUnlockId());
                }
            }
            case CHAT_COLOR -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveChatColor(item.getUnlockId());
            }
            case SPAWN_CAGE -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveSpawnCage(item.getUnlockId());
            }
            case PROJECTILE_TRAIL -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveProjectileTrail(item.getUnlockId());
            }
            case WING_PARTICLE -> {
                data.unlockCosmetic(item.getUnlockId());
                data.setActiveWingParticle(item.getUnlockId());
            }
            case JOIN_MESSAGE -> data.unlockCosmetic(item.getUnlockId());
            case BOOSTER -> {
                String[] parts = item.getUnlockId().split("_");
                double mult = 2.0;
                long minutes = 60;
                try {
                    if (parts.length >= 1) mult = Double.parseDouble(parts[0]);
                    if (parts.length >= 2) minutes = Long.parseLong(parts[1]);
                } catch (NumberFormatException ignored) {
                }
                data.setCoinMultiplier(mult, minutes * 60_000L);
            }
            case KIT_REROLL -> {
                int amount = 1;
                try {
                    amount = Math.max(1, Integer.parseInt(item.getUnlockId()));
                } catch (NumberFormatException ignored) {
                }
                data.addKitRerolls(amount);
            }
            case CRATE_KEY -> {
                String rarity = item.getUnlockId() == null ? "common" : item.getUnlockId();
                data.addCrateKeys(rarity, 1);
            }
        }
    }
}
