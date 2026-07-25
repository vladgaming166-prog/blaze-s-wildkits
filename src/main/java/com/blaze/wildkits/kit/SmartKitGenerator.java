package com.blaze.wildkits.kit;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Premium PvP-oriented WildKits generator.
 * Modern combat loadouts — enchanted weapons/armor, utilities, almost no survival clutter.
 */
public final class SmartKitGenerator {

    private static final Material[] PVP_FOOD = {
            Material.COOKED_BEEF, Material.GOLDEN_CARROT, Material.COOKED_PORKCHOP
    };
    private static final Material[] PVP_BLOCKS = {
            Material.COBBLESTONE, Material.OAK_PLANKS, Material.OBSIDIAN,
            Material.NETHERRACK, Material.END_STONE, Material.DEEPSLATE,
            Material.STONE, Material.DIRT
    };

    private final BlazesWildKits plugin;

    public SmartKitGenerator(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public GeneratedKit generate(KitDefinition definition) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        FileConfiguration cfg = plugin.getConfigManager().getConfig();
        GearBand band = rollBand(cfg, rng);
        String theme = definition.getProfile().getTheme().toLowerCase();

        ItemStack[] armor = buildArmor(band, theme, cfg, rng);
        enchantArmor(armor, band, cfg, rng);

        ItemStack weapon = buildWeapon(band, theme, cfg, rng);
        enchantWeapon(weapon, band, cfg, rng);

        List<ItemStack> extras = new ArrayList<>(32);
        applyThemeLoadout(definition, band, extras, rng, cfg);
        addCombatUtilities(extras, band, theme, cfg, rng);

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

    private GearBand rollBand(FileConfiguration cfg, ThreadLocalRandom rng) {
        double iron = cfg.getDouble("generation.iron-chance", 55.0);
        double diamond = cfg.getDouble("generation.diamond-chance", 32.0);
        double netherite = cfg.getDouble("generation.netherite-partial-chance", 10.0);
        double fullNetherite = cfg.getDouble("generation.full-netherite-chance", 3.0);
        double total = Math.max(0.01, iron + diamond + netherite + fullNetherite);
        double roll = rng.nextDouble() * total;
        if (roll < iron) return GearBand.IRON;
        if (roll < iron + diamond) return GearBand.DIAMOND;
        if (roll < iron + diamond + netherite) return GearBand.NETHERITE_PARTIAL;
        return GearBand.FULL_NETHERITE;
    }

    private ItemStack[] buildArmor(GearBand band, String theme, FileConfiguration cfg, ThreadLocalRandom rng) {
        ItemStack[] armor = new ItemStack[4];
        Material[] iron = {Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS};
        Material[] diamond = {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS};
        Material[] netherite = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
        int maxNetherite = Math.max(1, Math.min(3, cfg.getInt("generation.max-netherite-pieces", 2)));

        if (band == GearBand.IRON) {
            for (int i = 0; i < 4; i++) {
                armor[i] = new ItemStack(rng.nextDouble() < 0.28 ? diamond[i] : iron[i]);
            }
            // Classic iron/diamond mixes
            if (rng.nextDouble() < 0.25) {
                armor[1] = new ItemStack(Material.DIAMOND_CHESTPLATE);
            }
        } else if (band == GearBand.DIAMOND) {
            for (int i = 0; i < 4; i++) {
                armor[i] = new ItemStack(rng.nextDouble() < 0.08 ? iron[i] : diamond[i]);
            }
            if (rng.nextDouble() < 0.18) {
                armor[0] = new ItemStack(Material.IRON_HELMET);
                armor[1] = new ItemStack(Material.DIAMOND_CHESTPLATE);
            }
            // Occasional diamond + netherite piece
            if (rng.nextDouble() < 0.22) {
                armor[rng.nextInt(4)] = new ItemStack(netherite[rng.nextInt(4)]);
            }
        } else if (band == GearBand.NETHERITE_PARTIAL) {
            boolean[] nether = new boolean[4];
            int pieces = 1 + rng.nextInt(maxNetherite);
            int placed = 0;
            while (placed < pieces) {
                int idx = rng.nextInt(4);
                if (!nether[idx]) {
                    nether[idx] = true;
                    placed++;
                }
            }
            for (int i = 0; i < 4; i++) {
                armor[i] = new ItemStack(nether[i] ? netherite[i] : diamond[i]);
            }
        } else {
            // Rare full netherite
            for (int i = 0; i < 4; i++) {
                armor[i] = new ItemStack(netherite[i]);
            }
        }

        if ((theme.contains("assassin") || theme.contains("ninja") || theme.contains("shadow") || theme.contains("scout"))
                && rng.nextDouble() < 0.28) {
            armor[0] = new ItemStack(Material.AIR);
        }
        return armor;
    }

    private ItemStack buildWeapon(GearBand band, String theme, FileConfiguration cfg, ThreadLocalRandom rng) {
        if (rng.nextDouble() < cfg.getDouble("generation.mace-chance", 0.04)) {
            return new ItemStack(Material.MACE);
        }
        if (themeContains(theme, "pirate", "ocean", "thunder", "storm", "tide")
                || rng.nextDouble() < cfg.getDouble("generation.trident-chance", 0.07)) {
            return new ItemStack(Material.TRIDENT);
        }

        boolean axe = themeContains(theme, "viking", "lumber", "berserker", "lumberjack", "brawler")
                || rng.nextDouble() < 0.30;

        // Full netherite / partial prefer high-tier weapons
        if (band == GearBand.FULL_NETHERITE || (band == GearBand.NETHERITE_PARTIAL && rng.nextDouble() < 0.85)) {
            return new ItemStack(axe ? Material.NETHERITE_AXE : Material.NETHERITE_SWORD);
        }
        if (band == GearBand.DIAMOND || rng.nextDouble() < 0.82) {
            return new ItemStack(axe ? Material.DIAMOND_AXE : Material.DIAMOND_SWORD);
        }
        // Occasional lower-tier for variety / underdog kits
        double roll = rng.nextDouble();
        if (roll < 0.55) return new ItemStack(axe ? Material.IRON_AXE : Material.IRON_SWORD);
        if (roll < 0.80) return new ItemStack(axe ? Material.STONE_AXE : Material.STONE_SWORD);
        return new ItemStack(axe ? Material.WOODEN_AXE : Material.WOODEN_SWORD);
    }

    private void enchantArmor(ItemStack[] armor, GearBand band, FileConfiguration cfg, ThreadLocalRandom rng) {
        double chance = cfg.getDouble("generation.enchant-chance", 0.95);
        int maxProt = band == GearBand.FULL_NETHERITE ? 4 : (band == GearBand.NETHERITE_PARTIAL ? 3 : 2);
        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;
            if (rng.nextDouble() > chance) continue;
            piece.addUnsafeEnchantment(Enchantment.PROTECTION, 1 + rng.nextInt(maxProt));
            if (rng.nextDouble() < 0.60) {
                piece.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(3));
            }
            if (rng.nextDouble() < 0.18) {
                piece.addUnsafeEnchantment(Enchantment.PROJECTILE_PROTECTION, 1 + rng.nextInt(2));
            }
            if (rng.nextDouble() < 0.15) {
                piece.addUnsafeEnchantment(Enchantment.FIRE_PROTECTION, 1 + rng.nextInt(2));
            }
            if (rng.nextDouble() < 0.10) {
                piece.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 1);
            }
            if (piece.getType().name().contains("BOOTS")) {
                if (rng.nextDouble() < 0.35) {
                    piece.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 1 + rng.nextInt(3));
                }
                if (rng.nextDouble() < 0.20) {
                    piece.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 1 + rng.nextInt(2));
                }
                if (rng.nextDouble() < 0.12) {
                    piece.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 1);
                }
            }
            if (piece.getType().name().contains("HELMET") && rng.nextDouble() < 0.15) {
                piece.addUnsafeEnchantment(Enchantment.RESPIRATION, 1 + rng.nextInt(2));
            }
            if (band == GearBand.FULL_NETHERITE && rng.nextDouble() < 0.35) {
                piece.addUnsafeEnchantment(Enchantment.MENDING, 1);
            }
        }
    }

    private void enchantWeapon(ItemStack weapon, GearBand band, FileConfiguration cfg, ThreadLocalRandom rng) {
        if (weapon == null || weapon.getType().isAir()) return;
        if (rng.nextDouble() > cfg.getDouble("generation.enchant-chance", 0.95)) return;

        Material type = weapon.getType();
        int sharpMax = band == GearBand.FULL_NETHERITE ? 5 : (band == GearBand.NETHERITE_PARTIAL ? 4 : 3);

        if (type == Material.TRIDENT) {
            weapon.addUnsafeEnchantment(Enchantment.IMPALING, 1 + rng.nextInt(3));
            if (rng.nextBoolean()) weapon.addUnsafeEnchantment(Enchantment.LOYALTY, 1 + rng.nextInt(2));
            if (rng.nextDouble() < 0.35) weapon.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(2));
            if (rng.nextDouble() < 0.20) weapon.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
            return;
        }
        if (type == Material.MACE) {
            weapon.addUnsafeEnchantment(Enchantment.DENSITY, 1 + rng.nextInt(2));
            if (rng.nextBoolean()) weapon.addUnsafeEnchantment(Enchantment.BREACH, 1);
            if (rng.nextDouble() < 0.3) weapon.addUnsafeEnchantment(Enchantment.WIND_BURST, 1);
            return;
        }
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 1 + rng.nextInt(Math.max(1, sharpMax)));
        if (rng.nextDouble() < 0.50) weapon.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(3));
        if (rng.nextDouble() < 0.32) weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        if (rng.nextDouble() < 0.28) weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        if (rng.nextDouble() < 0.22) weapon.addUnsafeEnchantment(Enchantment.LOOTING, 1 + rng.nextInt(2));
        if (rng.nextDouble() < 0.15) weapon.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 1);
        if (band.ordinal() >= GearBand.NETHERITE_PARTIAL.ordinal() && rng.nextDouble() < 0.25) {
            weapon.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
    }

    private void applyThemeLoadout(KitDefinition def, GearBand band, List<ItemStack> extras,
                                   ThreadLocalRandom rng, FileConfiguration cfg) {
        String theme = def.getProfile().getTheme().toLowerCase();
        String id = def.getId().toLowerCase();

        if (themeContains(theme, "archer", "sniper", "hunter") || idContains(id, "bow", "crossbow", "archer", "sniper", "hunter")) {
            boolean cross = theme.contains("cross") || id.contains("cross") || rng.nextBoolean();
            ItemStack ranged = new ItemStack(cross ? Material.CROSSBOW : Material.BOW);
            if (cross) {
                ranged.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 1 + rng.nextInt(3));
                if (rng.nextDouble() < 0.40) ranged.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
                if (rng.nextDouble() < 0.30) ranged.addUnsafeEnchantment(Enchantment.PIERCING, 1 + rng.nextInt(2));
            } else {
                ranged.addUnsafeEnchantment(Enchantment.POWER, 1 + rng.nextInt(3));
                if (rng.nextDouble() < 0.45) ranged.addUnsafeEnchantment(Enchantment.PUNCH, 1);
                if (rng.nextDouble() < 0.25) ranged.addUnsafeEnchantment(Enchantment.FLAME, 1);
                if (rng.nextDouble() < 0.20) ranged.addUnsafeEnchantment(Enchantment.INFINITY, 1);
            }
            extras.add(ranged);
            extras.add(new ItemStack(Material.ARROW, range(cfg, "generation.arrows", 24, 64, rng)));
            if (rng.nextDouble() < 0.50) extras.add(new ItemStack(Material.SPECTRAL_ARROW, 8 + rng.nextInt(17)));
        }

        if (themeContains(theme, "tank", "knight", "guardian", "fortress", "gladiator", "royal", "sentinel", "spartan")) {
            extras.add(new ItemStack(Material.SHIELD));
            extras.add(new ItemStack(Material.GOLDEN_APPLE, 2 + rng.nextInt(4)));
            if (rng.nextDouble() < 0.45) extras.add(new ItemStack(Material.OBSIDIAN, 4 + rng.nextInt(5)));
        }

        if (themeContains(theme, "assassin", "ninja", "shadow", "scout", "speed", "rogue")) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 4 + rng.nextInt(5)));
            extras.add(splash(PotionType.SWIFTNESS));
            if (rng.nextDouble() < 0.45) extras.add(splash(PotionType.INVISIBILITY));
        }

        if (themeContains(theme, "bomber", "tnt", "trap", "chaos")) {
            extras.add(new ItemStack(Material.TNT, 2 + rng.nextInt(4)));
            extras.add(new ItemStack(Material.FLINT_AND_STEEL));
            extras.add(new ItemStack(Material.COBWEB, 2 + rng.nextInt(4)));
        }

        if (themeContains(theme, "lava", "nether", "pyro", "inferno", "phoenix", "blaze", "dragon")) {
            extras.add(new ItemStack(Material.FIRE_CHARGE, 8 + rng.nextInt(9)));
            extras.add(new ItemStack(Material.LAVA_BUCKET));
            extras.add(splash(PotionType.FIRE_RESISTANCE));
        }

        if (themeContains(theme, "ice", "frozen", "cryo", "snow")) {
            extras.add(new ItemStack(Material.SNOWBALL, 16));
            extras.add(splash(PotionType.SLOWNESS));
            extras.add(new ItemStack(Material.PACKED_ICE, 8));
        }

        if (themeContains(theme, "mage", "potion", "mystic", "crystal", "thunder", "storm", "magic")) {
            extras.add(splash(PotionType.STRENGTH));
            extras.add(splash(PotionType.SWIFTNESS));
            extras.add(splash(PotionType.HEALING));
            if (rng.nextDouble() < 0.55) extras.add(splash(PotionType.POISON));
        }

        if (themeContains(theme, "end", "void", "galaxy", "astral")) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 6 + rng.nextInt(3)));
            extras.add(new ItemStack(Material.CHORUS_FRUIT, 8));
        }

        if (themeContains(theme, "pirate", "ocean")) {
            extras.add(new ItemStack(Material.TRIDENT));
            extras.add(new ItemStack(Material.WATER_BUCKET));
            if (rng.nextDouble() < 0.5) extras.add(new ItemStack(Material.FISHING_ROD));
        }

        if (themeContains(theme, "builder", "bridge", "fortress")) {
            extras.add(new ItemStack(Material.OAK_PLANKS, 64));
            extras.add(new ItemStack(Material.COBBLESTONE, 64));
            extras.add(new ItemStack(Material.OBSIDIAN, 6 + rng.nextInt(7)));
            extras.add(new ItemStack(Material.WATER_BUCKET));
        }

        if (themeContains(theme, "miner")) {
            ItemStack pick = new ItemStack(band == GearBand.IRON ? Material.IRON_PICKAXE : Material.DIAMOND_PICKAXE);
            pick.addUnsafeEnchantment(Enchantment.EFFICIENCY, 1 + rng.nextInt(3));
            if (rng.nextDouble() < 0.25) pick.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
            else if (rng.nextDouble() < 0.35) pick.addUnsafeEnchantment(Enchantment.FORTUNE, 1);
            extras.add(pick);
            extras.add(new ItemStack(Material.COBBLESTONE, 32));
            extras.add(new ItemStack(Material.OBSIDIAN, 4));
        }
    }

    private void addCombatUtilities(List<ItemStack> extras, GearBand band, String theme,
                                    FileConfiguration cfg, ThreadLocalRandom rng) {
        extras.add(new ItemStack(PVP_FOOD[rng.nextInt(PVP_FOOD.length)], range(cfg, "generation.food", 16, 32, rng)));

        if (rng.nextDouble() < cfg.getDouble("generation.golden-apple-chance", 0.88)) {
            int gaps = 1 + rng.nextInt(band == GearBand.FULL_NETHERITE ? 5
                    : band == GearBand.NETHERITE_PARTIAL ? 4 : 3);
            extras.add(new ItemStack(Material.GOLDEN_APPLE, gaps));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.enchanted-golden-apple-chance", 0.06)) {
            extras.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        }

        if (rng.nextDouble() < cfg.getDouble("generation.blocks-chance", 0.88)) {
            extras.add(new ItemStack(PVP_BLOCKS[rng.nextInt(PVP_BLOCKS.length)], range(cfg, "generation.blocks", 16, 48, rng)));
        }

        if (rng.nextDouble() < cfg.getDouble("generation.water-bucket-chance", 0.75)) {
            extras.add(new ItemStack(Material.WATER_BUCKET));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.lava-bucket-chance", 0.38)) {
            extras.add(new ItemStack(Material.LAVA_BUCKET));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.ender-pearl-chance", 0.70)) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 2 + rng.nextInt(5)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.cobweb-chance", 0.50)) {
            extras.add(new ItemStack(Material.COBWEB, 1 + rng.nextInt(4)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.obsidian-chance", 0.38)) {
            extras.add(new ItemStack(Material.OBSIDIAN, 2 + rng.nextInt(7)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.tnt-chance", 0.22)) {
            extras.add(new ItemStack(Material.TNT, 1 + rng.nextInt(3)));
            extras.add(new ItemStack(Material.FLINT_AND_STEEL));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.fire-charge-chance", 0.35)) {
            extras.add(new ItemStack(Material.FIRE_CHARGE, 4 + rng.nextInt(9)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.wind-charge-chance", 0.32)) {
            extras.add(new ItemStack(Material.WIND_CHARGE, 2 + rng.nextInt(5)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.fishing-rod-chance", 0.28)) {
            ItemStack rod = new ItemStack(Material.FISHING_ROD);
            if (rng.nextDouble() < 0.45) rod.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
            extras.add(rod);
        }
        if (rng.nextDouble() < cfg.getDouble("generation.snowball-chance", 0.32)) {
            extras.add(new ItemStack(Material.SNOWBALL, 8 + rng.nextInt(17)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.egg-chance", 0.28)) {
            extras.add(new ItemStack(Material.EGG, 8 + rng.nextInt(9)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.shield-chance", 0.55)) {
            boolean has = extras.stream().anyMatch(i -> i.getType() == Material.SHIELD);
            if (!has) extras.add(new ItemStack(Material.SHIELD));
        }

        // Rare totem
        if (rng.nextDouble() < cfg.getDouble("generation.totem-chance", 0.08)
                || band == GearBand.FULL_NETHERITE && rng.nextDouble() < 0.35) {
            boolean has = extras.stream().anyMatch(i -> i.getType() == Material.TOTEM_OF_UNDYING);
            if (!has) extras.add(new ItemStack(Material.TOTEM_OF_UNDYING));
        }

        if (!themeContains(theme, "archer", "sniper", "hunter")
                && rng.nextDouble() < cfg.getDouble("generation.bow-chance", 0.48)) {
            ItemStack bow = new ItemStack(rng.nextBoolean() ? Material.BOW : Material.CROSSBOW);
            if (bow.getType() == Material.BOW) {
                bow.addUnsafeEnchantment(Enchantment.POWER, 1 + rng.nextInt(3));
                if (rng.nextDouble() < 0.35) bow.addUnsafeEnchantment(Enchantment.PUNCH, 1);
            } else {
                bow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 1 + rng.nextInt(2));
            }
            extras.add(bow);
            extras.add(new ItemStack(Material.ARROW, range(cfg, "generation.arrows", 16, 48, rng)));
        }

        if (rng.nextDouble() < cfg.getDouble("generation.potion-chance", 0.60)) {
            PotionType[] types = {
                    PotionType.STRENGTH, PotionType.SWIFTNESS, PotionType.HEALING,
                    PotionType.FIRE_RESISTANCE, PotionType.TURTLE_MASTER
            };
            extras.add(splash(types[rng.nextInt(types.length)]));
            if (rng.nextDouble() < 0.40) {
                extras.add(splash(types[rng.nextInt(types.length)]));
            }
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

    private int range(FileConfiguration cfg, String path, int defMin, int defMax, ThreadLocalRandom rng) {
        int min = cfg.getInt(path + "-min", defMin);
        int max = cfg.getInt(path + "-max", defMax);
        if (max < min) max = min;
        return rng.nextInt(min, max + 1);
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
