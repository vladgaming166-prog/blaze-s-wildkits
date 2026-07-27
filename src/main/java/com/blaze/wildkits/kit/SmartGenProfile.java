package com.blaze.wildkits.kit;

/**
 * Controls theme flavor for generation. Gear quality is driven by rarity → GearBand.
 */
public final class SmartGenProfile {

    private final double bowChance;
    private final double goldenAppleChance;
    private final double potionChance;
    private final double enchantChance;
    private final boolean preferRanged;
    private final boolean preferMagic;
    private final boolean preferAxe;
    private final String theme;

    public SmartGenProfile(double bowChance, double goldenAppleChance, double potionChance,
                           double enchantChance, boolean preferRanged, boolean preferMagic,
                           boolean preferAxe, String theme) {
        this.bowChance = clamp(bowChance);
        this.goldenAppleChance = clamp(goldenAppleChance);
        this.potionChance = clamp(potionChance);
        this.enchantChance = clamp(enchantChance);
        this.preferRanged = preferRanged;
        this.preferMagic = preferMagic;
        this.preferAxe = preferAxe;
        this.theme = theme == null ? "balanced" : theme;
    }

    public static SmartGenProfile forRarity(KitRarity rarity) {
        return switch (rarity) {
            case COMMON -> new SmartGenProfile(0.40, 0.70, 0.40, 0.92, false, false, false, "balanced");
            case UNCOMMON -> new SmartGenProfile(0.45, 0.75, 0.45, 0.93, false, false, false, "balanced");
            case RARE -> new SmartGenProfile(0.55, 0.82, 0.55, 0.95, false, false, false, "balanced");
            case EPIC -> new SmartGenProfile(0.60, 0.88, 0.65, 0.96, false, false, false, "balanced");
            case LEGENDARY -> new SmartGenProfile(0.70, 0.92, 0.75, 0.97, false, false, false, "balanced");
            case MYTHIC -> new SmartGenProfile(0.80, 0.95, 0.85, 0.98, false, false, false, "balanced");
            case GODLY, ULTIMATE -> new SmartGenProfile(0.90, 0.98, 0.95, 1.0, false, false, false, "balanced");
        };
    }

    public SmartGenProfile withTheme(String theme, boolean preferRanged, boolean preferMagic) {
        boolean axe = theme != null && (theme.contains("viking") || theme.contains("berserker")
                || theme.contains("axe") || theme.contains("brawler") || theme.contains("lumber"));
        return new SmartGenProfile(bowChance, goldenAppleChance, potionChance, enchantChance,
                preferRanged, preferMagic, axe, theme);
    }

    public double getBowChance() { return bowChance; }
    public double getGoldenAppleChance() { return goldenAppleChance; }
    public double getPotionChance() { return potionChance; }
    public double getEnchantChance() { return enchantChance; }
    public boolean isPreferRanged() { return preferRanged; }
    public boolean isPreferMagic() { return preferMagic; }
    public boolean isPreferAxe() { return preferAxe; }
    public String getTheme() { return theme; }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
