package com.blaze.wildkits.crate;

public enum CrateRarity {
    COMMON("<gray>Common", 50.0),
    RARE("<aqua>Rare", 25.0),
    EPIC("<light_purple>Epic", 15.0),
    LEGENDARY("<gold>Legendary", 8.0),
    MYTHIC("<red>Mythic", 2.0);

    private final String display;
    private final double defaultWeight;

    CrateRarity(String display, double defaultWeight) {
        this.display = display;
        this.defaultWeight = defaultWeight;
    }

    public String getDisplay() { return display; }
    public double getDefaultWeight() { return defaultWeight; }

    public static CrateRarity from(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return COMMON;
        }
    }
}
