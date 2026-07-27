package com.blaze.wildkits.kit;

import java.util.Locale;

/**
 * Premium rarity tiers. UNCOMMON/ULTIMATE kept for backwards-compatible kits.yml.
 */
public enum KitRarity {
    COMMON("<gray>Common", 38.0, 1, "<gray>"),
    UNCOMMON("<green>Uncommon", 18.0, 2, "<green>"),
    RARE("<aqua>Rare", 18.0, 3, "<aqua>"),
    EPIC("<light_purple>Epic", 12.0, 4, "<light_purple>"),
    LEGENDARY("<gold>Legendary", 8.0, 5, "<gold>"),
    MYTHIC("<red>Mythic", 4.0, 6, "<red>"),
    GODLY("<gradient:#FFD700:#FF4500><bold>Godly</bold></gradient>", 1.5, 7, "<gradient:#FFD700:#FF4500>"),
    ULTIMATE("<gradient:#ff006e:#8338ec>Ultimate", 0.5, 8, "<gradient:#ff006e:#8338ec>");

    private final String display;
    private final double defaultWeight;
    private final int tier;
    private final String color;

    KitRarity(String display, double defaultWeight, int tier, String color) {
        this.display = display;
        this.defaultWeight = defaultWeight;
        this.tier = tier;
        this.color = color;
    }

    public String getDisplay() { return display; }
    public double getDefaultWeight() { return defaultWeight; }
    public int getTier() { return tier; }
    public String getColor() { return color; }

    public static KitRarity fromString(String input) {
        if (input == null) return COMMON;
        try {
            return valueOf(input.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
