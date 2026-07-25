package com.blaze.wildkits.shop;

import org.bukkit.Material;

public final class ShopItem {

    public enum Type {
        KIT, KIT_UPGRADE, TRAIL, DEATH_EFFECT, VICTORY_EFFECT, KILL_EFFECT, TAG, TITLE
    }

    private final String id;
    private final String displayName;
    private final String description;
    private final Type type;
    private final Material icon;
    private final int price;
    private final String unlockId;
    private final String category;

    public ShopItem(String id, String displayName, String description, Type type,
                    Material icon, int price, String unlockId, String category) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.icon = icon == null ? Material.EMERALD : icon;
        this.price = Math.max(0, price);
        this.unlockId = unlockId == null ? id : unlockId;
        this.category = category == null ? "General" : category;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Type getType() { return type; }
    public Material getIcon() { return icon; }
    public int getPrice() { return price; }
    public String getUnlockId() { return unlockId; }
    public String getCategory() { return category; }
}
