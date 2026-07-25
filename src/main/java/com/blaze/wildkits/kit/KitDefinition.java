package com.blaze.wildkits.kit;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KitDefinition {

    private final String id;
    private final String displayName;
    private final String description;
    private final KitRarity rarity;
    private final Material icon;
    private final String permission;
    private final String category;
    private final boolean unlockedByDefault;
    private final int price;
    private final List<String> tags;
    private final SmartGenProfile profile;

    public KitDefinition(String id, String displayName, String description, KitRarity rarity,
                         Material icon, String permission, String category, boolean unlockedByDefault,
                         int price, List<String> tags, SmartGenProfile profile) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.icon = icon == null ? Material.CHEST : icon;
        this.permission = permission;
        this.category = category == null ? "General" : category;
        this.unlockedByDefault = unlockedByDefault;
        this.price = Math.max(0, price);
        this.tags = tags == null ? List.of() : List.copyOf(tags);
        this.profile = profile == null ? SmartGenProfile.forRarity(rarity) : profile;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public KitRarity getRarity() { return rarity; }
    public Material getIcon() { return icon; }
    public String getPermission() { return permission; }
    public String getCategory() { return category; }
    public boolean isUnlockedByDefault() { return unlockedByDefault; }
    public int getPrice() { return price; }
    public List<String> getTags() { return tags; }
    public SmartGenProfile getProfile() { return profile; }

    public List<String> loreLines() {
        List<String> lore = new ArrayList<>();
        lore.add("<gray>" + description);
        lore.add("");
        lore.add("<white>Rarity: " + rarity.getDisplay());
        lore.add("<white>Category: <yellow>" + category);
        if (price > 0 && !unlockedByDefault) {
            lore.add("<white>Price: <gold>" + price + " coins");
        }
        return Collections.unmodifiableList(lore);
    }
}
