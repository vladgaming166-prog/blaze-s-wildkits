package com.blaze.wildkits.kit;

import java.util.Locale;

public enum KitRarity {
    COMMON("<gray>Common", 40.0, 1),
    UNCOMMON("<green>Uncommon", 25.0, 2),
    RARE("<aqua>Rare", 15.0, 3),
    EPIC("<light_purple>Epic", 10.0, 4),
    LEGENDARY("<gold>Legendary", 6.0, 5),
    MYTHIC("<red>Mythic", 3.0, 6),
    ULTIMATE("<gradient:#ff006e:#8338ec>Ultimate", 1.0, 7);

    private final String display;
    private final double defaultWeight;
    private final int tier;

    KitRarity(String display, double defaultWeight, int tier) {
        this.display = display;
        this.defaultWeight = defaultWeight;
        this.tier = tier;
    }

    public String getDisplay() {
        return display;
    }

    public double getDefaultWeight() {
        return defaultWeight;
    }

    public int getTier() {
        return tier;
    }

    public static KitRarity fromString(String input) {
        if (input == null) return COMMON;
        try {
            return valueOf(input.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
