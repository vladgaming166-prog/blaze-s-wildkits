package com.blaze.wildkits.kit;

/**
 * Controls smart random equipment generation for a kit theme / rarity.
 * Tier 1 = weakest, Tier 5 = strongest (netherite-class).
 */
public final class SmartGenProfile {

    private final int minTier;
    private final int maxTier;
    private final double bowChance;
    private final double goldenAppleChance;
    private final double potionChance;
    private final double enchantChance;
    private final int foodMin;
    private final int foodMax;
    private final int blocksMin;
    private final int blocksMax;
    private final boolean preferRanged;
    private final boolean preferMagic;
    private final String theme;

    public SmartGenProfile(int minTier, int maxTier, double bowChance, double goldenAppleChance,
                           double potionChance, double enchantChance, int foodMin, int foodMax,
                           int blocksMin, int blocksMax, boolean preferRanged, boolean preferMagic,
                           String theme) {
        this.minTier = clamp(minTier, 1, 5);
        this.maxTier = Math.max(this.minTier, clamp(maxTier, 1, 5));
        this.bowChance = clampChance(bowChance);
        this.goldenAppleChance = clampChance(goldenAppleChance);
        this.potionChance = clampChance(potionChance);
        this.enchantChance = clampChance(enchantChance);
        this.foodMin = Math.max(0, foodMin);
        this.foodMax = Math.max(this.foodMin, foodMax);
        this.blocksMin = Math.max(0, blocksMin);
        this.blocksMax = Math.max(this.blocksMin, blocksMax);
        this.preferRanged = preferRanged;
        this.preferMagic = preferMagic;
        this.theme = theme == null ? "balanced" : theme;
    }

    public static SmartGenProfile forRarity(KitRarity rarity) {
        return switch (rarity) {
            case COMMON -> new SmartGenProfile(1, 2, 0.25, 0.10, 0.15, 0.20, 8, 16, 16, 32, false, false, "balanced");
            case UNCOMMON -> new SmartGenProfile(1, 3, 0.35, 0.20, 0.25, 0.35, 12, 20, 24, 40, false, false, "balanced");
            case RARE -> new SmartGenProfile(2, 3, 0.45, 0.35, 0.40, 0.50, 16, 24, 32, 48, false, false, "balanced");
            case EPIC -> new SmartGenProfile(2, 4, 0.55, 0.50, 0.55, 0.65, 20, 32, 40, 56, false, false, "balanced");
            case LEGENDARY -> new SmartGenProfile(3, 4, 0.65, 0.65, 0.70, 0.80, 24, 40, 48, 64, false, false, "balanced");
            case MYTHIC -> new SmartGenProfile(3, 5, 0.75, 0.80, 0.85, 0.90, 32, 48, 48, 64, false, false, "balanced");
            case ULTIMATE -> new SmartGenProfile(4, 5, 0.90, 0.95, 0.95, 1.0, 40, 64, 56, 64, false, false, "balanced");
        };
    }

    public SmartGenProfile withTheme(String theme, boolean preferRanged, boolean preferMagic) {
        return new SmartGenProfile(minTier, maxTier, bowChance, goldenAppleChance, potionChance,
                enchantChance, foodMin, foodMax, blocksMin, blocksMax, preferRanged, preferMagic, theme);
    }

    public int getMinTier() { return minTier; }
    public int getMaxTier() { return maxTier; }
    public double getBowChance() { return bowChance; }
    public double getGoldenAppleChance() { return goldenAppleChance; }
    public double getPotionChance() { return potionChance; }
    public double getEnchantChance() { return enchantChance; }
    public int getFoodMin() { return foodMin; }
    public int getFoodMax() { return foodMax; }
    public int getBlocksMin() { return blocksMin; }
    public int getBlocksMax() { return blocksMax; }
    public boolean isPreferRanged() { return preferRanged; }
    public boolean isPreferMagic() { return preferMagic; }
    public String getTheme() { return theme; }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double clampChance(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
