package com.blaze.wildkits.kit;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ultra-premium WildKits generator — large-server PvP quality.
 * Gear band is locked to kit rarity so combinations stay professional.
 */
public final class SmartKitGenerator {

    private static final Material[] FOOD = {
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.GOLDEN_CARROT
    };
    private static final Material[] BLOCKS = {
            Material.COBBLESTONE, Material.OAK_PLANKS, Material.OBSIDIAN,
            Material.END_STONE, Material.DEEPSLATE, Material.NETHERRACK, Material.STONE
    };

    private final BlazesWildKits plugin;

    public SmartKitGenerator(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public GeneratedKit generate(KitDefinition definition) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        KitRarity rarity = definition.getRarity();
        GearBand band = bandForRarity(rarity, rng);
        String theme = definition.getProfile().getTheme().toLowerCase();
        SmartGenProfile profile = definition.getProfile();

        ItemStack[] armor = buildArmor(band, theme, rng);
        enchantArmor(armor, band, rarity, profile.getEnchantChance(), rng);

        ItemStack weapon = buildWeapon(band, theme, profile, rng);
        enchantWeapon(weapon, band, rarity, profile.getEnchantChance(), rng);

        List<ItemStack> extras = new ArrayList<>(36);
        applyThemeExtras(definition, band, rarity, extras, rng);
        addCombatUtilities(extras, band, rarity, theme, profile, rng);
        enforceBalance(extras, band); // strip impossible leftovers if any

        return new GeneratedKit(definition, armor[0], armor[1], armor[2], armor[3], weapon, extras, band);
    }

    public void apply(PlayerInventory inventory, GeneratedKit kit) {
        inventory.clear();
        inventory.setHelmet(kit.helmet());
        inventory.setChestplate(kit.chestplate());
        inventory.setLeggings(kit.leggings());
        inventory.setBoots(kit.boots());

        ItemStack off = null;
        List<ItemStack> remaining = new ArrayList<>(kit.extras().size());
        for (ItemStack extra : kit.extras()) {
            if (extra == null) continue;
            if (off == null && (extra.getType() == Material.SHIELD || extra.getType() == Material.TOTEM_OF_UNDYING)) {
                off = extra;
            } else {
                remaining.add(extra);
            }
        }
        inventory.setItemInOffHand(off == null ? new ItemStack(Material.AIR) : off);
        inventory.setItem(0, kit.weapon());
        int slot = 1;
        for (ItemStack extra : remaining) {
            if (slot >= 36) break;
            inventory.setItem(slot++, extra);
        }
    }

    /** Rarity → gear band. No wood sword on netherite, no prot-IV netherite boots on iron sets. */
    private GearBand bandForRarity(KitRarity rarity, ThreadLocalRandom rng) {
        return switch (rarity) {
            case COMMON -> rng.nextDouble() < 0.78 ? GearBand.IRON : GearBand.DIAMOND;
            case UNCOMMON -> rng.nextDouble() < 0.45 ? GearBand.IRON : GearBand.DIAMOND;
            case RARE -> {
                double r = rng.nextDouble();
                if (r < 0.15) yield GearBand.IRON;
                if (r < 0.85) yield GearBand.DIAMOND;
                yield GearBand.NETHERITE_PARTIAL;
            }
            case EPIC -> rng.nextDouble() < 0.72 ? GearBand.DIAMOND : GearBand.NETHERITE_PARTIAL;
            case LEGENDARY -> {
                double r = rng.nextDouble();
                if (r < 0.55) yield GearBand.DIAMOND;
                if (r < 0.92) yield GearBand.NETHERITE_PARTIAL;
                yield GearBand.FULL_NETHERITE;
            }
            case MYTHIC -> {
                double r = rng.nextDouble();
                if (r < 0.35) yield GearBand.NETHERITE_PARTIAL;
                if (r < 0.85) yield GearBand.FULL_NETHERITE;
                yield GearBand.GODLY;
            }
            case GODLY, ULTIMATE -> rng.nextDouble() < 0.40 ? GearBand.FULL_NETHERITE : GearBand.GODLY;
        };
    }

    private ItemStack[] buildArmor(GearBand band, String theme, ThreadLocalRandom rng) {
        Material[] iron = {Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS};
        Material[] diamond = {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS};
        Material[] netherite = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
        ItemStack[] armor = new ItemStack[4];

        switch (band) {
            case IRON -> {
                for (int i = 0; i < 4; i++) {
                    // Occasional diamond piece upgrade — never netherite on iron band
                    armor[i] = new ItemStack(rng.nextDouble() < 0.22 ? diamond[i] : iron[i]);
                }
            }
            case DIAMOND -> {
                for (int i = 0; i < 4; i++) {
                    armor[i] = new ItemStack(diamond[i]);
                }
                // Classic mixes: iron helm + diamond rest, or one netherite piece
                if (rng.nextDouble() < 0.18) {
                    armor[0] = new ItemStack(Material.IRON_HELMET);
                }
                if (rng.nextDouble() < 0.20) {
                    armor[rng.nextInt(4)] = new ItemStack(netherite[rng.nextInt(4)]);
                }
            }
            case NETHERITE_PARTIAL -> {
                boolean[] n = new boolean[4];
                int pieces = 1 + rng.nextInt(2); // 1-2 pieces
                for (int p = 0; p < pieces; p++) {
                    int idx = rng.nextInt(4);
                    while (n[idx]) idx = rng.nextInt(4);
                    n[idx] = true;
                }
                for (int i = 0; i < 4; i++) {
                    armor[i] = new ItemStack(n[i] ? netherite[i] : diamond[i]);
                }
            }
            case FULL_NETHERITE, GODLY -> {
                for (int i = 0; i < 4; i++) {
                    armor[i] = new ItemStack(netherite[i]);
                }
            }
        }

        if ((themeContains(theme, "assassin", "ninja", "shadow", "scout")) && rng.nextDouble() < 0.22
                && band.ordinal() <= GearBand.DIAMOND.ordinal()) {
            armor[0] = new ItemStack(Material.AIR);
        }
        return armor;
    }

    private ItemStack buildWeapon(GearBand band, String theme, SmartGenProfile profile, ThreadLocalRandom rng) {
        // Rare special weapons (band-gated)
        if (band.ordinal() >= GearBand.NETHERITE_PARTIAL.ordinal() && rng.nextDouble() < 0.05) {
            return new ItemStack(Material.MACE);
        }
        if (themeContains(theme, "pirate", "ocean", "thunder", "storm")
                || (band.ordinal() >= GearBand.DIAMOND.ordinal() && rng.nextDouble() < 0.06)) {
            return new ItemStack(Material.TRIDENT);
        }

        boolean axe = profile.isPreferAxe() || themeContains(theme, "viking", "berserker", "brawler", "lumber")
                || rng.nextDouble() < 0.28;

        // Weapon ALWAYS matches band quality — never wooden on netherite
        return switch (band) {
            case IRON -> {
                if (rng.nextDouble() < 0.70) {
                    yield new ItemStack(axe ? Material.IRON_AXE : Material.IRON_SWORD);
                }
                // Occasional diamond weapon upgrade on iron armor (still valid)
                yield new ItemStack(axe ? Material.DIAMOND_AXE : Material.DIAMOND_SWORD);
            }
            case DIAMOND -> new ItemStack(axe ? Material.DIAMOND_AXE : Material.DIAMOND_SWORD);
            case NETHERITE_PARTIAL -> rng.nextDouble() < 0.75
                    ? new ItemStack(axe ? Material.NETHERITE_AXE : Material.NETHERITE_SWORD)
                    : new ItemStack(axe ? Material.DIAMOND_AXE : Material.DIAMOND_SWORD);
            case FULL_NETHERITE, GODLY -> new ItemStack(axe ? Material.NETHERITE_AXE : Material.NETHERITE_SWORD);
        };
    }

    private void enchantArmor(ItemStack[] armor, GearBand band, KitRarity rarity,
                              double enchantChance, ThreadLocalRandom rng) {
        int maxProt = switch (band) {
            case IRON -> 2;
            case DIAMOND -> 3;
            case NETHERITE_PARTIAL -> 3;
            case FULL_NETHERITE -> 4;
            case GODLY -> 4;
        };
        // Cap protection by rarity so iron kits never roll Prot IV
        if (rarity.ordinal() <= KitRarity.COMMON.ordinal()) maxProt = Math.min(maxProt, 2);
        else if (rarity.ordinal() <= KitRarity.RARE.ordinal()) maxProt = Math.min(maxProt, 3);

        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;
            if (rng.nextDouble() > enchantChance) continue;

            piece.addUnsafeEnchantment(Enchantment.PROTECTION, 1 + rng.nextInt(maxProt));
            if (rng.nextDouble() < 0.65) {
                piece.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(Math.min(3, maxProt)));
            }
            if (rng.nextDouble() < 0.18) {
                piece.addUnsafeEnchantment(Enchantment.PROJECTILE_PROTECTION, 1 + rng.nextInt(2));
            }
            if (rng.nextDouble() < 0.15) {
                piece.addUnsafeEnchantment(Enchantment.FIRE_PROTECTION, 1 + rng.nextInt(2));
            }
            if (rng.nextDouble() < 0.10) {
                piece.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 1 + rng.nextInt(2));
            }
            if (piece.getType().name().contains("BOOTS")) {
                if (rng.nextDouble() < 0.45) {
                    piece.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 1 + rng.nextInt(4));
                }
                if (rng.nextDouble() < 0.30) {
                    piece.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 1 + rng.nextInt(3));
                }
                if (band.ordinal() >= GearBand.DIAMOND.ordinal() && rng.nextDouble() < 0.15) {
                    piece.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 1 + rng.nextInt(2));
                }
            }
            if (piece.getType().name().contains("HELMET") && rng.nextDouble() < 0.20) {
                piece.addUnsafeEnchantment(Enchantment.RESPIRATION, 1 + rng.nextInt(3));
                if (rng.nextDouble() < 0.4) piece.addUnsafeEnchantment(Enchantment.AQUA_AFFINITY, 1);
            }
            double mendChance = switch (band) {
                case IRON -> 0.02;
                case DIAMOND -> 0.08;
                case NETHERITE_PARTIAL -> 0.18;
                case FULL_NETHERITE -> 0.35;
                case GODLY -> 0.55;
            };
            if (rng.nextDouble() < mendChance) {
                piece.addUnsafeEnchantment(Enchantment.MENDING, 1);
            }
        }
    }

    private void enchantWeapon(ItemStack weapon, GearBand band, KitRarity rarity,
                               double enchantChance, ThreadLocalRandom rng) {
        if (weapon == null || weapon.getType().isAir()) return;
        if (rng.nextDouble() > enchantChance) return;

        int sharpMax = switch (band) {
            case IRON -> 2;
            case DIAMOND -> 3;
            case NETHERITE_PARTIAL -> 4;
            case FULL_NETHERITE -> 5;
            case GODLY -> 5;
        };
        if (rarity == KitRarity.COMMON) sharpMax = Math.min(sharpMax, 2);
        if (rarity == KitRarity.RARE) sharpMax = Math.min(sharpMax, 3);

        Material type = weapon.getType();
        if (type == Material.TRIDENT) {
            weapon.addUnsafeEnchantment(Enchantment.IMPALING, 1 + rng.nextInt(Math.min(5, sharpMax + 1)));
            if (rng.nextBoolean()) weapon.addUnsafeEnchantment(Enchantment.LOYALTY, 1 + rng.nextInt(3));
            if (rng.nextDouble() < 0.35) weapon.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
            if (rng.nextDouble() < 0.40) weapon.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(3));
            return;
        }
        if (type == Material.MACE) {
            weapon.addUnsafeEnchantment(Enchantment.DENSITY, 1 + rng.nextInt(3));
            if (rng.nextBoolean()) weapon.addUnsafeEnchantment(Enchantment.BREACH, 1 + rng.nextInt(2));
            if (rng.nextDouble() < 0.35) weapon.addUnsafeEnchantment(Enchantment.WIND_BURST, 1);
            return;
        }

        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 1 + rng.nextInt(Math.max(1, sharpMax)));
        if (rng.nextDouble() < 0.55) weapon.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(3));
        if (rng.nextDouble() < 0.32) weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        if (rng.nextDouble() < 0.30) weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1 + (band.ordinal() >= GearBand.NETHERITE_PARTIAL.ordinal() && rng.nextBoolean() ? 1 : 0));
        if (rng.nextDouble() < 0.22) weapon.addUnsafeEnchantment(Enchantment.LOOTING, 1 + rng.nextInt(2));
        if (type.name().contains("SWORD") && rng.nextDouble() < 0.18) {
            weapon.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 1 + rng.nextInt(2));
        }
        double mend = band.ordinal() >= GearBand.NETHERITE_PARTIAL.ordinal() ? 0.28 : 0.08;
        if (band == GearBand.GODLY) mend = 0.50;
        if (rng.nextDouble() < mend) weapon.addUnsafeEnchantment(Enchantment.MENDING, 1);
    }

    private void applyThemeExtras(KitDefinition def, GearBand band, KitRarity rarity,
                                  List<ItemStack> extras, ThreadLocalRandom rng) {
        String theme = def.getProfile().getTheme().toLowerCase();
        String id = def.getId().toLowerCase();

        if (themeContains(theme, "archer", "sniper", "hunter") || idContains(id, "bow", "cross", "archer", "sniper")) {
            boolean cross = theme.contains("cross") || id.contains("cross") || rng.nextBoolean();
            ItemStack ranged = new ItemStack(cross ? Material.CROSSBOW : Material.BOW);
            enchantRanged(ranged, band, rng);
            extras.add(ranged);
            extras.add(new ItemStack(Material.ARROW, 24 + rng.nextInt(41)));
            if (rng.nextDouble() < 0.55) extras.add(tippedArrow(rng));
            if (rng.nextDouble() < 0.40) extras.add(new ItemStack(Material.SPECTRAL_ARROW, 8 + rng.nextInt(17)));
        }

        if (themeContains(theme, "tank", "knight", "guardian", "sentinel", "spartan", "paladin")) {
            extras.add(new ItemStack(Material.SHIELD));
            extras.add(new ItemStack(Material.GOLDEN_APPLE, 2 + rng.nextInt(3 + rarity.getTier() / 2)));
            if (rng.nextDouble() < 0.45) extras.add(new ItemStack(Material.OBSIDIAN, 4 + rng.nextInt(8)));
        }

        if (themeContains(theme, "assassin", "ninja", "shadow", "scout", "rogue", "speed")) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 4 + rng.nextInt(5)));
            extras.add(splash(PotionType.SWIFTNESS));
            if (rng.nextDouble() < 0.40) extras.add(splash(PotionType.INVISIBILITY));
        }

        if (themeContains(theme, "bomber", "tnt", "trap", "chaos")) {
            extras.add(new ItemStack(Material.TNT, 2 + rng.nextInt(4)));
            extras.add(new ItemStack(Material.FLINT_AND_STEEL));
            extras.add(new ItemStack(Material.COBWEB, 2 + rng.nextInt(4)));
        }

        if (themeContains(theme, "lava", "nether", "pyro", "phoenix", "blaze", "inferno")) {
            extras.add(new ItemStack(Material.FIRE_CHARGE, 8 + rng.nextInt(9)));
            extras.add(new ItemStack(Material.LAVA_BUCKET));
            extras.add(splash(PotionType.FIRE_RESISTANCE));
        }

        if (themeContains(theme, "mage", "potion", "mystic", "crystal", "magic", "alchemist")) {
            extras.add(splash(PotionType.STRENGTH));
            extras.add(splash(PotionType.SWIFTNESS));
            extras.add(splash(PotionType.HEALING));
            if (rng.nextDouble() < 0.5) extras.add(splash(PotionType.POISON));
        }

        if (themeContains(theme, "builder", "bridge")) {
            extras.add(new ItemStack(Material.OAK_PLANKS, 64));
            extras.add(new ItemStack(Material.COBBLESTONE, 64));
            extras.add(new ItemStack(Material.OBSIDIAN, 8 + rng.nextInt(9)));
            extras.add(new ItemStack(Material.WATER_BUCKET));
        }

        if (themeContains(theme, "end", "void", "galaxy", "astral")) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 6 + rng.nextInt(3)));
            extras.add(new ItemStack(Material.CHORUS_FRUIT, 8));
        }

        if (themeContains(theme, "pirate", "ocean")) {
            extras.add(new ItemStack(Material.WATER_BUCKET));
            if (rng.nextDouble() < 0.5) extras.add(new ItemStack(Material.FISHING_ROD));
        }
    }

    private void addCombatUtilities(List<ItemStack> extras, GearBand band, KitRarity rarity,
                                    String theme, SmartGenProfile profile, ThreadLocalRandom rng) {
        extras.add(new ItemStack(FOOD[rng.nextInt(FOOD.length)], 16 + rng.nextInt(17)));

        // Gaps scale with rarity
        double gapChance = Math.max(profile.getGoldenAppleChance(), 0.70 + rarity.getTier() * 0.03);
        if (rng.nextDouble() < gapChance) {
            int amount = 1 + rng.nextInt(2 + rarity.getTier() / 2);
            extras.add(new ItemStack(Material.GOLDEN_APPLE, amount));
        }
        // Enchanted golden apple — very rare
        double gapple = switch (band) {
            case IRON -> 0.01;
            case DIAMOND -> 0.03;
            case NETHERITE_PARTIAL -> 0.06;
            case FULL_NETHERITE -> 0.12;
            case GODLY -> 0.25;
        };
        if (rng.nextDouble() < gapple) {
            extras.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        }

        if (rng.nextDouble() < 0.88) {
            extras.add(new ItemStack(BLOCKS[rng.nextInt(BLOCKS.length)], 16 + rng.nextInt(49)));
        }
        if (rng.nextDouble() < 0.75) extras.add(new ItemStack(Material.WATER_BUCKET));
        if (rng.nextDouble() < 0.38) extras.add(new ItemStack(Material.LAVA_BUCKET));
        if (rng.nextDouble() < 0.70) extras.add(new ItemStack(Material.ENDER_PEARL, 2 + rng.nextInt(5)));
        if (rng.nextDouble() < 0.48) extras.add(new ItemStack(Material.COBWEB, 1 + rng.nextInt(4)));
        if (rng.nextDouble() < 0.40) extras.add(new ItemStack(Material.OBSIDIAN, 2 + rng.nextInt(8)));
        if (rng.nextDouble() < 0.20) {
            extras.add(new ItemStack(Material.TNT, 1 + rng.nextInt(3)));
            extras.add(new ItemStack(Material.FLINT_AND_STEEL));
        }
        if (rng.nextDouble() < 0.35) extras.add(new ItemStack(Material.FIRE_CHARGE, 4 + rng.nextInt(9)));
        if (rng.nextDouble() < 0.35) extras.add(new ItemStack(Material.WIND_CHARGE, 2 + rng.nextInt(6)));
        if (rng.nextDouble() < 0.30) {
            ItemStack rod = new ItemStack(Material.FISHING_ROD);
            if (rng.nextDouble() < 0.45) rod.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
            extras.add(rod);
        }
        if (rng.nextDouble() < 0.30) extras.add(new ItemStack(Material.SNOWBALL, 8 + rng.nextInt(17)));
        if (rng.nextDouble() < 0.55 && extras.stream().noneMatch(i -> i.getType() == Material.SHIELD)) {
            extras.add(new ItemStack(Material.SHIELD));
        }

        // Totem — rare, scales with band
        double totem = switch (band) {
            case IRON -> 0.02;
            case DIAMOND -> 0.05;
            case NETHERITE_PARTIAL -> 0.12;
            case FULL_NETHERITE -> 0.28;
            case GODLY -> 0.55;
        };
        if (rng.nextDouble() < totem && extras.stream().noneMatch(i -> i.getType() == Material.TOTEM_OF_UNDYING)) {
            extras.add(new ItemStack(Material.TOTEM_OF_UNDYING));
        }

        if (!themeContains(theme, "archer", "sniper", "hunter")
                && rng.nextDouble() < Math.max(0.40, profile.getBowChance())) {
            ItemStack bow = new ItemStack(rng.nextBoolean() ? Material.BOW : Material.CROSSBOW);
            enchantRanged(bow, band, rng);
            extras.add(bow);
            extras.add(new ItemStack(Material.ARROW, 16 + rng.nextInt(33)));
            if (rng.nextDouble() < 0.35) extras.add(tippedArrow(rng));
        }

        if (rng.nextDouble() < Math.max(0.45, profile.getPotionChance())) {
            PotionType[] types = {
                    PotionType.STRENGTH, PotionType.SWIFTNESS, PotionType.HEALING,
                    PotionType.FIRE_RESISTANCE, PotionType.TURTLE_MASTER
            };
            extras.add(splash(types[rng.nextInt(types.length)]));
            if (rarity.getTier() >= 4 && rng.nextDouble() < 0.45) {
                extras.add(splash(types[rng.nextInt(types.length)]));
            }
        }
    }

    private void enchantRanged(ItemStack ranged, GearBand band, ThreadLocalRandom rng) {
        int powerMax = band.ordinal() >= GearBand.NETHERITE_PARTIAL.ordinal() ? 5 : 3;
        if (ranged.getType() == Material.BOW) {
            ranged.addUnsafeEnchantment(Enchantment.POWER, 1 + rng.nextInt(powerMax));
            if (rng.nextDouble() < 0.40) ranged.addUnsafeEnchantment(Enchantment.PUNCH, 1);
            if (rng.nextDouble() < 0.25) ranged.addUnsafeEnchantment(Enchantment.FLAME, 1);
            if (rng.nextDouble() < 0.18) ranged.addUnsafeEnchantment(Enchantment.INFINITY, 1);
            else if (rng.nextDouble() < 0.15) ranged.addUnsafeEnchantment(Enchantment.MENDING, 1);
            if (rng.nextDouble() < 0.40) ranged.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(3));
        } else {
            ranged.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 1 + rng.nextInt(3));
            if (rng.nextDouble() < 0.40) ranged.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
            if (rng.nextDouble() < 0.35) ranged.addUnsafeEnchantment(Enchantment.PIERCING, 1 + rng.nextInt(3));
            if (rng.nextDouble() < 0.35) ranged.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(3));
        }
    }

    private void enforceBalance(List<ItemStack> extras, GearBand band) {
        // No-op placeholder — armor/weapon already band-locked.
        // Keep list sane size
        while (extras.size() > 32) {
            extras.remove(extras.size() - 1);
        }
    }

    private ItemStack splash(PotionType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        if (item.getItemMeta() instanceof PotionMeta meta) {
            meta.setBasePotionType(type);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack tippedArrow(ThreadLocalRandom rng) {
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW, 4 + rng.nextInt(9));
        if (arrow.getItemMeta() instanceof PotionMeta meta) {
            PotionType[] types = {PotionType.SLOWNESS, PotionType.POISON, PotionType.WEAKNESS, PotionType.HEALING};
            meta.setBasePotionType(types[rng.nextInt(types.length)]);
            arrow.setItemMeta(meta);
        }
        return arrow;
    }

    private boolean themeContains(String theme, String... keys) {
        for (String key : keys) {
            if (theme.contains(key)) return true;
        }
        return false;
    }

    private boolean idContains(String id, String... keys) {
        for (String key : keys) {
            if (id.contains(key)) return true;
        }
        return false;
    }

    public record GeneratedKit(
            KitDefinition definition,
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            ItemStack weapon,
            List<ItemStack> extras,
            GearBand band
    ) {
        public List<ItemStack> allPreviewItems() {
            List<ItemStack> all = new ArrayList<>(8 + extras.size());
            if (helmet != null && !helmet.getType().isAir()) all.add(helmet.clone());
            if (chestplate != null) all.add(chestplate.clone());
            if (leggings != null) all.add(leggings.clone());
            if (boots != null) all.add(boots.clone());
            if (weapon != null) all.add(weapon.clone());
            for (ItemStack extra : extras) {
                if (extra != null) all.add(extra.clone());
            }
            return all;
        }
    }
}
