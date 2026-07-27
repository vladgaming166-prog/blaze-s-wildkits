package com.blaze.wildkits.kit;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * 250+ handcrafted PvP kit templates. Each still randomizes gear via SmartKitGenerator.
 */
public final class DefaultKits {

    private DefaultKits() {}

    public static List<KitDefinition> createAll() {
        List<KitDefinition> kits = new ArrayList<>(320);

        // Signature named kits
        add(kits, "starter", "Iron Recruit", "Reliable iron opener", KitRarity.COMMON, Material.IRON_SWORD, "Melee", true, 0, "balanced", false, false);
        add(kits, "warrior", "Diamond Warrior", "Diamond melee pressure", KitRarity.COMMON, Material.DIAMOND_SWORD, "Melee", true, 0, "balanced", false, false);
        add(kits, "knight", "Shield Knight", "Frontline shield fighter", KitRarity.UNCOMMON, Material.SHIELD, "Melee", true, 0, "knight", false, false);
        add(kits, "tank", "Iron Tank", "Heavy defense kit", KitRarity.UNCOMMON, Material.IRON_CHESTPLATE, "Melee", true, 0, "tank", false, false);
        add(kits, "assassin", "Pearl Assassin", "Pearls and burst damage", KitRarity.RARE, Material.ENDER_PEARL, "Stealth", false, 500, "assassin", false, false);
        add(kits, "archer", "Power Archer", "Bow dominance", KitRarity.COMMON, Material.BOW, "Ranged", true, 0, "archer", true, false);
        add(kits, "mage", "Potion Mage", "Splash control kit", KitRarity.RARE, Material.SPLASH_POTION, "Magic", false, 750, "mage", false, true);
        add(kits, "hunter", "Crossbow Hunter", "Crossbow pursuit", KitRarity.UNCOMMON, Material.CROSSBOW, "Ranged", true, 0, "hunter", true, false);
        add(kits, "builder", "Bridge Builder", "Blocks and cover", KitRarity.COMMON, Material.OAK_PLANKS, "Utility", true, 0, "builder", false, false);
        add(kits, "pvp", "PvP Striker", "Classic diamond PvP", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "Melee", true, 0, "balanced", false, false);
        add(kits, "nether", "Nether Raider", "Fireproof nether assault", KitRarity.RARE, Material.NETHERRACK, "Elemental", false, 800, "nether", false, false);
        add(kits, "end", "End Fighter", "Pearls and chorus", KitRarity.RARE, Material.ENDER_PEARL, "Elemental", false, 900, "end", false, false);
        add(kits, "diamond", "Diamond Assault", "Full diamond pressure", KitRarity.EPIC, Material.DIAMOND_CHESTPLATE, "Elite", false, 1500, "balanced", false, false);
        add(kits, "netherite", "Partial Netherite", "Rare netherite fragments", KitRarity.LEGENDARY, Material.NETHERITE_SWORD, "Elite", false, 3000, "balanced", false, false);
        add(kits, "godly_blade", "Godly Blade", "God-tier enchanted arms", KitRarity.GODLY, Material.NETHER_STAR, "Elite", false, 12000, "champion", false, false);
        add(kits, "op", "Chaos Overload", "Showcase chaos kit", KitRarity.ULTIMATE, Material.NETHER_STAR, "Elite", false, 10000, "chaos", false, false);
        add(kits, "legend", "Legend Blade", "Legendary presence", KitRarity.LEGENDARY, Material.GOLDEN_HELMET, "Elite", false, 4000, "champion", false, false);
        add(kits, "mythic", "Mythic Edge", "Mythic-grade arms", KitRarity.MYTHIC, Material.DRAGON_EGG, "Elite", false, 6000, "mystic", false, false);
        add(kits, "champion", "Arena Champion", "Tournament kit", KitRarity.EPIC, Material.DIAMOND_HELMET, "Elite", false, 2000, "champion", false, false);
        add(kits, "lava", "Lava Bomber", "Lava and fire charges", KitRarity.RARE, Material.LAVA_BUCKET, "Elemental", false, 850, "lava", false, false);
        add(kits, "ice", "Ice Mage", "Frost control", KitRarity.RARE, Material.PACKED_ICE, "Elemental", false, 850, "ice", false, true);
        add(kits, "storm", "Thunder Storm", "Trident tempest", KitRarity.EPIC, Material.TRIDENT, "Elemental", false, 1600, "thunder", false, true);
        add(kits, "speed", "Speed Runner", "Swift mobility", KitRarity.UNCOMMON, Material.SUGAR, "Mobility", true, 0, "speed", false, false);
        add(kits, "sniper", "Longbow Sniper", "Precision arrows", KitRarity.RARE, Material.ARROW, "Ranged", false, 700, "sniper", true, false);
        add(kits, "samurai", "Samurai Blade", "Blade discipline", KitRarity.EPIC, Material.DIAMOND_SWORD, "Melee", false, 1700, "samurai", false, false);
        add(kits, "pirate", "Ocean Pirate", "Trident sea kit", KitRarity.UNCOMMON, Material.TRIDENT, "Special", true, 0, "pirate", false, false);
        add(kits, "ninja", "Shadow Ninja", "Silent lethal kit", KitRarity.RARE, Material.BLACK_DYE, "Stealth", false, 900, "ninja", false, false);
        add(kits, "viking", "Viking Raider", "Axe fury", KitRarity.UNCOMMON, Material.DIAMOND_AXE, "Melee", true, 0, "viking", false, false);
        add(kits, "gladiator", "Gladiator", "Arena brawler", KitRarity.UNCOMMON, Material.IRON_CHESTPLATE, "Melee", true, 0, "gladiator", false, false);
        add(kits, "scout", "Scout Runner", "Light recon kit", KitRarity.COMMON, Material.LEATHER_BOOTS, "Mobility", true, 0, "scout", false, false);
        add(kits, "berserker", "Berserker", "Aggressive axe rush", KitRarity.RARE, Material.DIAMOND_AXE, "Melee", false, 650, "berserker", false, false);
        add(kits, "paladin", "Guardian Paladin", "Holy defender", KitRarity.EPIC, Material.GOLDEN_APPLE, "Melee", false, 1900, "guardian", false, false);
        add(kits, "rogue", "Rogue Duelist", "Shadowy duelist", KitRarity.RARE, Material.ENDER_EYE, "Stealth", false, 800, "assassin", false, false);
        add(kits, "ranger", "Wild Ranger", "Wilderness marksman", KitRarity.UNCOMMON, Material.BOW, "Ranged", true, 0, "hunter", true, false);
        add(kits, "warlock", "Warlock", "Dark magic kit", KitRarity.EPIC, Material.SOUL_LANTERN, "Magic", false, 2100, "mage", false, true);
        add(kits, "phoenix", "Phoenix", "Ash rebirth kit", KitRarity.MYTHIC, Material.BLAZE_POWDER, "Elemental", false, 7000, "phoenix", false, false);
        add(kits, "shadow", "Shadow", "Umbral assassin", KitRarity.LEGENDARY, Material.BLACK_CONCRETE, "Stealth", false, 4200, "shadow", false, false);
        add(kits, "warden", "Deep Warden", "Deep dark terror", KitRarity.MYTHIC, Material.SCULK_SHRIEKER, "Elite", false, 8000, "tank", false, false);
        add(kits, "bomber", "TNT Expert", "Explosive pressure", KitRarity.RARE, Material.TNT, "Special", false, 1100, "bomber", false, false);
        add(kits, "duelist", "Duelist", "1v1 specialist", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "Melee", true, 0, "balanced", false, false);
        add(kits, "brawler", "Axe Brawler", "Close-quarters axe", KitRarity.COMMON, Material.DIAMOND_AXE, "Melee", true, 0, "viking", false, false);
        add(kits, "sentinel", "Shield Sentinel", "Watchful defender", KitRarity.RARE, Material.SHIELD, "Melee", false, 900, "tank", false, false);
        add(kits, "reaper", "Shadow Reaper", "Harvest souls", KitRarity.MYTHIC, Material.NETHERITE_HOE, "Stealth", false, 7500, "shadow", false, false);
        add(kits, "alchemist", "Potion Master", "Potion specialist", KitRarity.RARE, Material.BREWING_STAND, "Magic", false, 1200, "potion", false, true);
        add(kits, "totem_king", "Totem King", "Rare totem defense", KitRarity.GODLY, Material.TOTEM_OF_UNDYING, "Elite", false, 15000, "tank", false, false);

        // Mass template pools (handcrafted themes + numbered variants)
        expand(kits, "blade", "Blade", "Enchanted sword pressure", KitRarity.COMMON, Material.IRON_SWORD, "Melee", true, 0, "balanced", false, false, 18);
        expand(kits, "edge", "Edge", "Diamond edge fighter", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "Melee", true, 0, "balanced", false, false, 16);
        expand(kits, "strike", "Strike", "Aggressive melee", KitRarity.RARE, Material.DIAMOND_SWORD, "Melee", false, 400, "balanced", false, false, 14);
        expand(kits, "fury", "Fury", "Axe fury kit", KitRarity.RARE, Material.DIAMOND_AXE, "Melee", false, 500, "berserker", false, false, 14);
        expand(kits, "clash", "Clash", "Close combat kit", KitRarity.UNCOMMON, Material.IRON_AXE, "Melee", true, 0, "brawler", false, false, 12);
        expand(kits, "duel", "Duel", "1v1 diamond kit", KitRarity.EPIC, Material.DIAMOND_SWORD, "Melee", false, 1200, "balanced", false, false, 12);
        expand(kits, "rift", "Rift", "Pearl rift fighter", KitRarity.RARE, Material.ENDER_PEARL, "Stealth", false, 700, "assassin", false, false, 12);
        expand(kits, "pulse", "Pulse", "Speed pulse kit", KitRarity.UNCOMMON, Material.SUGAR, "Mobility", true, 0, "speed", false, false, 10);
        expand(kits, "nova", "Nova", "Explosive nova kit", KitRarity.EPIC, Material.FIRE_CHARGE, "Special", false, 1400, "bomber", false, false, 10);
        expand(kits, "apex", "Apex", "Apex predator kit", KitRarity.LEGENDARY, Material.NETHERITE_SWORD, "Elite", false, 2800, "champion", false, false, 10);

        expand(kits, "bowman", "Bowman", "Power bow kit", KitRarity.COMMON, Material.BOW, "Ranged", true, 0, "archer", true, false, 16);
        expand(kits, "marks", "Marksman", "Precision marksman", KitRarity.UNCOMMON, Material.CROSSBOW, "Ranged", true, 0, "hunter", true, false, 14);
        expand(kits, "arrow", "Arrow Storm", "Arrow spam kit", KitRarity.RARE, Material.ARROW, "Ranged", false, 600, "sniper", true, false, 12);
        expand(kits, "cross", "Cross Rush", "Crossbow rush", KitRarity.RARE, Material.CROSSBOW, "Ranged", false, 650, "hunter", true, false, 12);
        expand(kits, "snipe", "Snipe", "Long-range sniper", KitRarity.EPIC, Material.BOW, "Ranged", false, 1500, "sniper", true, false, 10);

        expand(kits, "guard", "Guard", "Shield defense", KitRarity.COMMON, Material.SHIELD, "Melee", true, 0, "tank", false, false, 12);
        expand(kits, "fort", "Fortress", "Heavy fortress kit", KitRarity.RARE, Material.OBSIDIAN, "Melee", false, 800, "fortress", false, false, 10);
        expand(kits, "totem", "Totem Guard", "Totem chance kit", KitRarity.EPIC, Material.TOTEM_OF_UNDYING, "Melee", false, 1800, "tank", false, false, 12);
        expand(kits, "web", "Web Trap", "Cobweb control", KitRarity.RARE, Material.COBWEB, "Special", false, 600, "trap", false, false, 10);
        expand(kits, "lava_c", "Lava Combo", "Lava bucket PvP", KitRarity.RARE, Material.LAVA_BUCKET, "Elemental", false, 700, "lava", false, false, 10);
        expand(kits, "wind", "Wind Fighter", "Wind charge fighter", KitRarity.EPIC, Material.WIND_CHARGE, "Special", false, 1200, "speed", false, false, 10);
        expand(kits, "mace", "Mace Rare", "Rare mace template", KitRarity.LEGENDARY, Material.MACE, "Elite", false, 3500, "champion", false, false, 8);

        expand(kits, "pot", "Pot Fighter", "Strength/speed pots", KitRarity.EPIC, Material.SPLASH_POTION, "Magic", false, 1600, "potion", false, true, 12);
        expand(kits, "arcane", "Arcane", "Magic barrage", KitRarity.LEGENDARY, Material.ENCHANTED_BOOK, "Magic", false, 3200, "mage", false, true, 8);
        expand(kits, "bridge", "Bridge Fight", "Blocks + pearls", KitRarity.UNCOMMON, Material.OAK_PLANKS, "Utility", true, 0, "builder", false, false, 10);
        expand(kits, "nether_m", "Nether Mix", "Diamond + netherite mix", KitRarity.LEGENDARY, Material.NETHERITE_CHESTPLATE, "Elite", false, 3200, "nether", false, false, 10);
        expand(kits, "full_n", "Full Netherite", "Full netherite roll", KitRarity.MYTHIC, Material.NETHERITE_HELMET, "Elite", false, 7000, "champion", false, false, 8);
        expand(kits, "god", "Godly Set", "Godly-tier roll", KitRarity.GODLY, Material.NETHERITE_SWORD, "Elite", false, 10000, "champion", false, false, 6);

        expand(kits, "ghost", "Ghost", "Hard to pin", KitRarity.EPIC, Material.GHAST_TEAR, "Stealth", false, 2200, "ninja", false, false, 8);
        expand(kits, "vamp", "Vampire", "Life-steal vibes", KitRarity.LEGENDARY, Material.REDSTONE_BLOCK, "Stealth", false, 5000, "assassin", false, false, 6);
        expand(kits, "frost", "Frostbite", "Deep freeze", KitRarity.EPIC, Material.BLUE_ICE, "Elemental", false, 1750, "frozen", false, false, 8);
        expand(kits, "thunder", "Thunder", "Lightning kit", KitRarity.EPIC, Material.LIGHTNING_ROD, "Elemental", false, 1800, "thunder", false, true, 8);
        expand(kits, "ocean", "Ocean", "Ocean fighter", KitRarity.UNCOMMON, Material.PRISMARINE_SHARD, "Special", true, 0, "ocean", false, false, 8);
        expand(kits, "crimson", "Crimson", "Crimson nether", KitRarity.RARE, Material.CRIMSON_NYLIUM, "Elemental", false, 1000, "nether", false, false, 8);
        expand(kits, "void", "Void Walker", "End void kit", KitRarity.EPIC, Material.ENDER_EYE, "Elemental", false, 2000, "end", false, false, 8);
        expand(kits, "royal", "Royal Guard", "Royal defender", KitRarity.LEGENDARY, Material.GOLDEN_CHESTPLATE, "Melee", false, 4500, "royal", false, false, 6);
        expand(kits, "spartan", "Spartan", "Shield spear vibe", KitRarity.RARE, Material.SHIELD, "Melee", false, 900, "spartan", false, false, 8);
        expand(kits, "elite_g", "Elite Guard", "Elite standard", KitRarity.EPIC, Material.DIAMOND_BOOTS, "Elite", false, 1800, "knight", false, false, 8);

        // Ensure we exceed 250
        expand(kits, "pvp_e", "PvP Elite", "Premium melee kit", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "Melee", true, 0, "balanced", false, false, 20);
        expand(kits, "pearl_c", "Pearl Combo", "Pearl + gap modern PvP", KitRarity.RARE, Material.ENDER_PEARL, "Stealth", false, 800, "assassin", false, false, 15);
        expand(kits, "axe_r", "Axe Rush", "Diamond/netherite axe", KitRarity.RARE, Material.DIAMOND_AXE, "Melee", false, 650, "berserker", false, false, 15);

        return kits;
    }

    private static void expand(List<KitDefinition> kits, String prefix, String name, String desc,
                               KitRarity rarity, Material icon, String category, boolean unlocked,
                               int price, String theme, boolean ranged, boolean magic, int count) {
        for (int i = 1; i <= count; i++) {
            add(kits, prefix + "_" + i, name + " #" + i, desc, rarity, icon, category, unlocked, price, theme, ranged, magic);
        }
    }

    private static void add(List<KitDefinition> kits, String id, String name, String desc, KitRarity rarity,
                            Material icon, String category, boolean unlocked, int price, String theme,
                            boolean ranged, boolean magic) {
        SmartGenProfile profile = SmartGenProfile.forRarity(rarity).withTheme(theme, ranged, magic);
        kits.add(new KitDefinition(
                id,
                "<gradient:#FF4500:#FFD700>" + name + "</gradient>",
                desc,
                rarity,
                icon,
                "wildkits.kit." + id,
                category,
                unlocked,
                price,
                List.of(category.toLowerCase(), rarity.name().toLowerCase(), theme),
                profile
        ));
    }
}
