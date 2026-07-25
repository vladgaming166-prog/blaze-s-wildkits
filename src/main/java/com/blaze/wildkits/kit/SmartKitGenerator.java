package com.blaze.wildkits.kit;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Balanced smart random generator.
 * Armor tier and weapon tier stay within 1 step of each other.
 */
public final class SmartKitGenerator {

    private static final Material[][] HELMETS = {
            {Material.LEATHER_HELMET},
            {Material.CHAINMAIL_HELMET, Material.GOLDEN_HELMET},
            {Material.IRON_HELMET},
            {Material.DIAMOND_HELMET},
            {Material.NETHERITE_HELMET}
    };
    private static final Material[][] CHESTPLATES = {
            {Material.LEATHER_CHESTPLATE},
            {Material.CHAINMAIL_CHESTPLATE, Material.GOLDEN_CHESTPLATE},
            {Material.IRON_CHESTPLATE},
            {Material.DIAMOND_CHESTPLATE},
            {Material.NETHERITE_CHESTPLATE}
    };
    private static final Material[][] LEGGINGS = {
            {Material.LEATHER_LEGGINGS},
            {Material.CHAINMAIL_LEGGINGS, Material.GOLDEN_LEGGINGS},
            {Material.IRON_LEGGINGS},
            {Material.DIAMOND_LEGGINGS},
            {Material.NETHERITE_LEGGINGS}
    };
    private static final Material[][] BOOTS = {
            {Material.LEATHER_BOOTS},
            {Material.CHAINMAIL_BOOTS, Material.GOLDEN_BOOTS},
            {Material.IRON_BOOTS},
            {Material.DIAMOND_BOOTS},
            {Material.NETHERITE_BOOTS}
    };
    private static final Material[][] SWORDS = {
            {Material.WOODEN_SWORD},
            {Material.STONE_SWORD, Material.GOLDEN_SWORD},
            {Material.IRON_SWORD},
            {Material.DIAMOND_SWORD},
            {Material.NETHERITE_SWORD}
    };
    private static final Material[][] AXES = {
            {Material.WOODEN_AXE},
            {Material.STONE_AXE, Material.GOLDEN_AXE},
            {Material.IRON_AXE},
            {Material.DIAMOND_AXE},
            {Material.NETHERITE_AXE}
    };

    private static final Material[] FOODS = {
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_CHICKEN,
            Material.BREAD, Material.GOLDEN_CARROT, Material.COOKED_SALMON,
            Material.BAKED_POTATO, Material.APPLE, Material.COOKED_MUTTON
    };

    private static final Material[] BLOCKS = {
            Material.COBBLESTONE, Material.OAK_PLANKS, Material.STONE_BRICKS,
            Material.DIRT, Material.NETHERRACK, Material.END_STONE,
            Material.OBSIDIAN, Material.SANDSTONE, Material.BRICKS
    };

    public GeneratedKit generate(KitDefinition definition) {
        SmartGenProfile profile = definition.getProfile();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        int armorTier = rng.nextInt(profile.getMinTier(), profile.getMaxTier() + 1);
        // Keep weapons within ±1 of armor tier for balance
        int weaponMin = Math.max(profile.getMinTier(), armorTier - 1);
        int weaponMax = Math.min(profile.getMaxTier(), armorTier + 1);
        int weaponTier = rng.nextInt(weaponMin, weaponMax + 1);

        ItemStack helmet = piece(HELMETS, armorTier, rng);
        ItemStack chest = piece(CHESTPLATES, armorTier, rng);
        ItemStack legs = piece(LEGGINGS, armorTier, rng);
        ItemStack boots = piece(BOOTS, armorTier, rng);

        boolean useAxe = "viking".equalsIgnoreCase(profile.getTheme())
                || "lumber".equalsIgnoreCase(profile.getTheme())
                || rng.nextDouble() < 0.18;
        ItemStack weapon = useAxe ? piece(AXES, weaponTier, rng) : piece(SWORDS, weaponTier, rng);

        maybeEnchantArmor(helmet, chest, legs, boots, profile, rng);
        maybeEnchantWeapon(weapon, profile, rng);

        List<ItemStack> extras = new ArrayList<>();

        boolean giveBow = profile.isPreferRanged() || rng.nextDouble() < profile.getBowChance();
        if (giveBow) {
            ItemStack bow = new ItemStack(Material.BOW);
            if (rng.nextDouble() < profile.getEnchantChance()) {
                bow.addUnsafeEnchantment(Enchantment.POWER, 1 + rng.nextInt(Math.max(1, weaponTier)));
                if (weaponTier >= 3 && rng.nextBoolean()) {
                    bow.addUnsafeEnchantment(Enchantment.PUNCH, 1);
                }
                if (weaponTier >= 4 && rng.nextDouble() < 0.4) {
                    bow.addUnsafeEnchantment(Enchantment.FLAME, 1);
                }
            }
            extras.add(bow);
            extras.add(new ItemStack(Material.ARROW, 16 + rng.nextInt(33)));
            if (weaponTier >= 3 && rng.nextDouble() < 0.35) {
                extras.add(new ItemStack(Material.SPECTRAL_ARROW, 8 + rng.nextInt(9)));
            }
        }

        if (profile.isPreferMagic() || rng.nextDouble() < profile.getPotionChance()) {
            extras.add(splash(PotionType.STRENGTH, rng.nextBoolean()));
            if (armorTier >= 3) {
                extras.add(splash(PotionType.SWIFTNESS, false));
            }
            if (armorTier >= 4 && rng.nextBoolean()) {
                extras.add(splash(PotionType.FIRE_RESISTANCE, false));
            }
            extras.add(new ItemStack(Material.POTION));
            applyDrinkable(extras.get(extras.size() - 1), PotionType.HEALING);
        }

        if (rng.nextDouble() < profile.getGoldenAppleChance()) {
            int apples = 1 + rng.nextInt(Math.max(1, armorTier));
            extras.add(new ItemStack(Material.GOLDEN_APPLE, apples));
            if (armorTier >= 5 && rng.nextDouble() < 0.25) {
                extras.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
            }
        }

        int foodAmount = rng.nextInt(profile.getFoodMin(), profile.getFoodMax() + 1);
        extras.add(new ItemStack(FOODS[rng.nextInt(FOODS.length)], Math.min(64, foodAmount)));

        int blockAmount = rng.nextInt(profile.getBlocksMin(), profile.getBlocksMax() + 1);
        Material block = themeBlock(profile.getTheme(), rng);
        extras.add(new ItemStack(block, Math.min(64, blockAmount)));

        applyThemeExtras(definition, extras, armorTier, weaponTier, rng);

        // Shield chance scales with tankiness
        if (armorTier >= 2 && rng.nextDouble() < 0.35 + armorTier * 0.08) {
            extras.add(new ItemStack(Material.SHIELD));
        }

        return new GeneratedKit(definition, helmet, chest, legs, boots, weapon, extras, armorTier, weaponTier);
    }

    public void apply(PlayerInventory inventory, GeneratedKit kit) {
        inventory.clear();
        inventory.setHelmet(kit.helmet());
        inventory.setChestplate(kit.chestplate());
        inventory.setLeggings(kit.leggings());
        inventory.setBoots(kit.boots());
        inventory.setItem(0, kit.weapon());
        int slot = 1;
        for (ItemStack extra : kit.extras()) {
            if (slot >= 36) break;
            inventory.setItem(slot++, extra);
        }
    }

    private void applyThemeExtras(KitDefinition def, List<ItemStack> extras, int armorTier, int weaponTier, ThreadLocalRandom rng) {
        String theme = def.getProfile().getTheme().toLowerCase();
        switch (theme) {
            case "archer", "sniper", "hunter" -> {
                extras.add(new ItemStack(Material.ARROW, 32));
                if (!extras.stream().anyMatch(i -> i.getType() == Material.BOW || i.getType() == Material.CROSSBOW)) {
                    ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                    crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, Math.min(3, weaponTier));
                    extras.add(crossbow);
                }
            }
            case "miner" -> {
                ItemStack pick = new ItemStack(tierTool(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE,
                        Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE, armorTier));
                pick.addUnsafeEnchantment(Enchantment.EFFICIENCY, Math.min(5, armorTier + 1));
                extras.add(pick);
                extras.add(new ItemStack(Material.TORCH, 32));
            }
            case "builder" -> {
                extras.add(new ItemStack(Material.OAK_PLANKS, 64));
                extras.add(new ItemStack(Material.SCAFFOLDING, 32));
            }
            case "nether", "lava" -> {
                extras.add(splash(PotionType.FIRE_RESISTANCE, false));
                extras.add(new ItemStack(Material.FIRE_CHARGE, 8));
            }
            case "ice", "snow" -> {
                extras.add(new ItemStack(Material.PACKED_ICE, 16));
                extras.add(new ItemStack(Material.SNOWBALL, 16));
            }
            case "mage", "magic" -> {
                extras.add(splash(PotionType.POISON, true));
                extras.add(splash(PotionType.SLOWNESS, true));
                extras.add(new ItemStack(Material.ENDER_PEARL, 2 + rng.nextInt(3)));
            }
            case "assassin", "ninja" -> {
                extras.add(new ItemStack(Material.ENDER_PEARL, 4));
                extras.add(splash(PotionType.INVISIBILITY, false));
            }
            case "tank", "knight" -> {
                extras.add(new ItemStack(Material.SHIELD));
                extras.add(new ItemStack(Material.GOLDEN_APPLE, 2));
            }
            case "pirate" -> {
                extras.add(new ItemStack(Material.TRIDENT));
                extras.add(new ItemStack(Material.COOKED_COD, 16));
            }
            case "farmer" -> {
                extras.add(new ItemStack(Material.GOLDEN_CARROT, 16));
                extras.add(new ItemStack(Material.HAY_BLOCK, 8));
            }
            case "end" -> {
                extras.add(new ItemStack(Material.ENDER_PEARL, 8));
                extras.add(new ItemStack(Material.CHORUS_FRUIT, 16));
            }
            case "speed" -> extras.add(splash(PotionType.SWIFTNESS, true));
            default -> {
                if (rng.nextDouble() < 0.2) {
                    extras.add(new ItemStack(Material.ENDER_PEARL, 1 + rng.nextInt(2)));
                }
            }
        }
    }

    private Material themeBlock(String theme, ThreadLocalRandom rng) {
        return switch (theme.toLowerCase()) {
            case "nether", "lava" -> Material.NETHERRACK;
            case "end" -> Material.END_STONE;
            case "ice", "snow" -> Material.PACKED_ICE;
            case "builder" -> Material.OAK_PLANKS;
            case "miner" -> Material.COBBLESTONE;
            default -> BLOCKS[rng.nextInt(BLOCKS.length)];
        };
    }

    private ItemStack piece(Material[][] table, int tier, ThreadLocalRandom rng) {
        int index = Math.max(0, Math.min(table.length - 1, tier - 1));
        Material[] options = table[index];
        return new ItemStack(options[rng.nextInt(options.length)]);
    }

    private Material tierTool(Material t1, Material t2, Material t3, Material t4, Material t5, int tier) {
        return switch (Math.max(1, Math.min(5, tier))) {
            case 1 -> t1;
            case 2 -> t2;
            case 3 -> t3;
            case 4 -> t4;
            default -> t5;
        };
    }

    private void maybeEnchantArmor(ItemStack h, ItemStack c, ItemStack l, ItemStack b,
                                   SmartGenProfile profile, ThreadLocalRandom rng) {
        if (rng.nextDouble() > profile.getEnchantChance()) return;
        int level = 1 + rng.nextInt(Math.max(1, profile.getMaxTier() - 1));
        for (ItemStack piece : List.of(h, c, l, b)) {
            piece.addUnsafeEnchantment(Enchantment.PROTECTION, Math.min(4, level));
            if (rng.nextDouble() < 0.3) {
                piece.addUnsafeEnchantment(Enchantment.UNBREAKING, Math.min(3, level));
            }
        }
        if (profile.getTheme().equalsIgnoreCase("lava") || profile.getTheme().equalsIgnoreCase("nether")) {
            for (ItemStack piece : List.of(h, c, l, b)) {
                piece.addUnsafeEnchantment(Enchantment.FIRE_PROTECTION, Math.min(4, level));
            }
        }
    }

    private void maybeEnchantWeapon(ItemStack weapon, SmartGenProfile profile, ThreadLocalRandom rng) {
        if (rng.nextDouble() > profile.getEnchantChance()) return;
        int level = 1 + rng.nextInt(Math.max(1, profile.getMaxTier()));
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, Math.min(5, level));
        if (rng.nextDouble() < 0.35) {
            weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        }
        if (rng.nextDouble() < 0.25) {
            weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        }
        if (rng.nextDouble() < 0.4) {
            weapon.addUnsafeEnchantment(Enchantment.UNBREAKING, Math.min(3, level));
        }
    }

    private ItemStack splash(PotionType type, boolean upgraded) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(upgraded && type != PotionType.INVISIBILITY ? upgrade(type) : type);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void applyDrinkable(ItemStack item, PotionType type) {
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return;
        meta.setBasePotionType(type);
        item.setItemMeta(meta);
    }

    private PotionType upgrade(PotionType type) {
        return switch (type) {
            case STRENGTH -> PotionType.STRENGTH;
            case SWIFTNESS -> PotionType.SWIFTNESS;
            case HEALING -> PotionType.HEALING;
            default -> type;
        };
    }

    public record GeneratedKit(
            KitDefinition definition,
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            ItemStack weapon,
            List<ItemStack> extras,
            int armorTier,
            int weaponTier
    ) {
        public List<ItemStack> allPreviewItems() {
            List<ItemStack> all = new ArrayList<>();
            all.add(helmet.clone());
            all.add(chestplate.clone());
            all.add(leggings.clone());
            all.add(boots.clone());
            all.add(weapon.clone());
            for (ItemStack extra : extras) {
                all.add(extra.clone());
            }
            return all;
        }
    }
}
