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
 * Premium WildKits generator.
 * Gear bands: Iron ~60%, Diamond ~35%, Netherite-partial ~5%.
 * Never grants a full netherite armor set. Same template => different extras.
 */
public final class SmartKitGenerator {

    private static final Material[] FOODS = {
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.GOLDEN_CARROT,
            Material.COOKED_CHICKEN, Material.BREAD, Material.COOKED_MUTTON, Material.COOKED_SALMON
    };
    private static final Material[] BLOCKS = {
            Material.COBBLESTONE, Material.OAK_PLANKS, Material.STONE_BRICKS, Material.DIRT,
            Material.NETHERRACK, Material.END_STONE, Material.SANDSTONE, Material.BRICKS,
            Material.DEEPSLATE, Material.BLACKSTONE, Material.CHERRY_PLANKS
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
        ItemStack weapon = buildWeapon(band, theme, definition, cfg, rng);
        maybeEnchantArmor(armor, cfg, rng);
        maybeEnchantWeapon(weapon, cfg, rng);

        List<ItemStack> extras = new ArrayList<>(24);
        applyThemeLoadout(definition, band, extras, rng, cfg);
        addRandomUtilities(extras, band, theme, cfg, rng);

        return new GeneratedKit(definition, armor[0], armor[1], armor[2], armor[3], weapon, extras, band);
    }

    public void apply(PlayerInventory inventory, GeneratedKit kit) {
        inventory.clear();
        inventory.setHelmet(kit.helmet());
        inventory.setChestplate(kit.chestplate());
        inventory.setLeggings(kit.leggings());
        inventory.setBoots(kit.boots());
        inventory.setItemInOffHand(pickOffhand(kit));
        inventory.setItem(0, kit.weapon());
        int slot = 1;
        for (ItemStack extra : kit.extras()) {
            if (extra == null) continue;
            if (slot >= 36) break;
            // Skip if we already put this as offhand
            if (extra.getType() == Material.SHIELD && inventory.getItemInOffHand() != null
                    && inventory.getItemInOffHand().getType() == Material.SHIELD) {
                continue;
            }
            inventory.setItem(slot++, extra);
        }
    }

    private ItemStack pickOffhand(GeneratedKit kit) {
        for (ItemStack extra : kit.extras()) {
            if (extra != null && extra.getType() == Material.SHIELD) {
                return extra.clone();
            }
        }
        return new ItemStack(Material.AIR);
    }

    private GearBand rollBand(FileConfiguration cfg, ThreadLocalRandom rng) {
        double iron = cfg.getDouble("generation.iron-chance", 60.0);
        double diamond = cfg.getDouble("generation.diamond-chance", 35.0);
        double netherite = cfg.getDouble("generation.netherite-partial-chance", 5.0);
        double total = Math.max(0.01, iron + diamond + netherite);
        double roll = rng.nextDouble() * total;
        if (roll < iron) return GearBand.IRON;
        if (roll < iron + diamond) return GearBand.DIAMOND;
        return GearBand.NETHERITE_PARTIAL;
    }

    private ItemStack[] buildArmor(GearBand band, String theme, FileConfiguration cfg, ThreadLocalRandom rng) {
        ItemStack[] armor = new ItemStack[4];
        int maxNetherite = Math.max(1, Math.min(3, cfg.getInt("generation.max-netherite-pieces", 2)));

        Material[] iron = {
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS
        };
        Material[] diamond = {
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
        };
        Material[] netherite = {
                Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS
        };

        if (band == GearBand.IRON) {
            for (int i = 0; i < 4; i++) {
                // Mostly iron; occasional diamond piece for spice
                armor[i] = new ItemStack(rng.nextDouble() < 0.18 ? diamond[i] : iron[i]);
            }
        } else if (band == GearBand.DIAMOND) {
            for (int i = 0; i < 4; i++) {
                armor[i] = new ItemStack(rng.nextDouble() < 0.12 ? iron[i] : diamond[i]);
            }
        } else {
            // Partial netherite: 1..maxNetherite pieces, rest diamond. NEVER full set.
            int pieces = 1 + rng.nextInt(maxNetherite);
            boolean[] nether = new boolean[4];
            int placed = 0;
            while (placed < pieces) {
                int idx = rng.nextInt(4);
                if (!nether[idx]) {
                    nether[idx] = true;
                    placed++;
                }
            }
            // Guarantee at least one diamond piece remains
            if (pieces >= 4) {
                nether[rng.nextInt(4)] = false;
            }
            for (int i = 0; i < 4; i++) {
                armor[i] = new ItemStack(nether[i] ? netherite[i] : diamond[i]);
            }
        }

        // Theme flavor: tank prefers heavier chest; assassin may drop helmet for mobility feel
        if ("assassin".equals(theme) || "ninja".equals(theme) || "shadow".equals(theme)) {
            if (rng.nextDouble() < 0.35) {
                armor[0] = new ItemStack(Material.AIR);
            }
        }
        if ("archer".equals(theme) || "sniper".equals(theme) || "hunter".equals(theme)) {
            // Prefer boots/legs over heavy chest sometimes
            if (rng.nextDouble() < 0.25 && band != GearBand.NETHERITE_PARTIAL) {
                armor[1] = new ItemStack(band == GearBand.IRON ? Material.IRON_CHESTPLATE : Material.DIAMOND_CHESTPLATE);
            }
        }
        return armor;
    }

    private ItemStack buildWeapon(GearBand band, String theme, KitDefinition def, FileConfiguration cfg, ThreadLocalRandom rng) {
        // Prefer diamond/netherite weapons — almost no wood/stone
        boolean preferAxe = themeContains(theme, "viking", "lumber", "berserker", "lumberjack")
                || "axe".equalsIgnoreCase(def.getProfile().getTheme());
        boolean preferTrident = themeContains(theme, "pirate", "ocean", "thunder", "storm", "tide");

        if (preferTrident && rng.nextDouble() < 0.7) {
            ItemStack trident = new ItemStack(Material.TRIDENT);
            if (rng.nextDouble() < cfg.getDouble("generation.enchant-chance", 0.45)) {
                trident.addUnsafeEnchantment(Enchantment.IMPALING, 1 + rng.nextInt(2));
                if (rng.nextBoolean()) trident.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
            }
            return trident;
        }

        Material material;
        if (band == GearBand.NETHERITE_PARTIAL && rng.nextDouble() < 0.75) {
            material = preferAxe ? Material.NETHERITE_AXE : Material.NETHERITE_SWORD;
        } else if (band == GearBand.DIAMOND || rng.nextDouble() < 0.85) {
            material = preferAxe ? Material.DIAMOND_AXE : Material.DIAMOND_SWORD;
        } else {
            material = preferAxe ? Material.IRON_AXE : Material.IRON_SWORD;
        }
        // Rare spicy rolls
        if (rng.nextDouble() < 0.08) {
            material = Material.DIAMOND_AXE;
        }
        return new ItemStack(material);
    }

    private void applyThemeLoadout(KitDefinition def, GearBand band, List<ItemStack> extras,
                                   ThreadLocalRandom rng, FileConfiguration cfg) {
        String theme = def.getProfile().getTheme().toLowerCase();
        String id = def.getId().toLowerCase();

        if (themeContains(theme, "archer", "sniper", "hunter") || idContains(id, "bow", "archer", "sniper", "hunter")) {
            ItemStack bow = new ItemStack(rng.nextDouble() < 0.45 ? Material.CROSSBOW : Material.BOW);
            if (rng.nextDouble() < cfg.getDouble("generation.enchant-chance", 0.45)) {
                if (bow.getType() == Material.BOW) {
                    bow.addUnsafeEnchantment(Enchantment.POWER, 1 + rng.nextInt(2));
                    if (rng.nextBoolean()) bow.addUnsafeEnchantment(Enchantment.PUNCH, 1);
                } else {
                    bow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 1 + rng.nextInt(2));
                    if (rng.nextBoolean()) bow.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
                }
            }
            extras.add(bow);
            extras.add(new ItemStack(Material.ARROW, range(cfg, "generation.arrows", 24, 64, rng)));
            if (rng.nextDouble() < 0.4) {
                extras.add(new ItemStack(Material.SPECTRAL_ARROW, 8 + rng.nextInt(17)));
            }
        }

        if (themeContains(theme, "tank", "knight", "guardian", "fortress", "gladiator", "royal")) {
            extras.add(new ItemStack(Material.SHIELD));
            extras.add(new ItemStack(Material.GOLDEN_APPLE, 1 + rng.nextInt(band == GearBand.IRON ? 2 : 3)));
        }

        if (themeContains(theme, "assassin", "ninja", "shadow", "scout", "speed")) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 2 + rng.nextInt(5)));
            if (rng.nextDouble() < 0.5) extras.add(splash(PotionType.SWIFTNESS, true));
            if (rng.nextDouble() < 0.35) extras.add(splash(PotionType.INVISIBILITY, false));
        }

        if (themeContains(theme, "miner")) {
            ItemStack pick = new ItemStack(band == GearBand.NETHERITE_PARTIAL ? Material.NETHERITE_PICKAXE
                    : band == GearBand.DIAMOND ? Material.DIAMOND_PICKAXE : Material.IRON_PICKAXE);
            if (rng.nextDouble() < 0.6) {
                pick.addUnsafeEnchantment(Enchantment.EFFICIENCY, 1 + rng.nextInt(2));
                if (rng.nextDouble() < 0.35) pick.addUnsafeEnchantment(Enchantment.FORTUNE, 1);
            }
            extras.add(pick);
            extras.add(new ItemStack(Material.TORCH, 32 + rng.nextInt(33)));
            extras.add(new ItemStack(Material.COBBLESTONE, 32 + rng.nextInt(33)));
        }

        if (themeContains(theme, "builder", "bridge", "fortress")) {
            extras.add(new ItemStack(Material.OAK_PLANKS, 48 + rng.nextInt(17)));
            extras.add(new ItemStack(Material.SCAFFOLDING, 24 + rng.nextInt(25)));
            if (rng.nextDouble() < 0.5) extras.add(new ItemStack(Material.OBSIDIAN, 4 + rng.nextInt(9)));
        }

        if (themeContains(theme, "lumber", "viking", "lumberjack")) {
            ItemStack axe = new ItemStack(band == GearBand.IRON ? Material.IRON_AXE : Material.DIAMOND_AXE);
            if (rng.nextBoolean()) axe.addUnsafeEnchantment(Enchantment.EFFICIENCY, 1 + rng.nextInt(2));
            extras.add(axe);
            extras.add(new ItemStack(Material.OAK_LOG, 16 + rng.nextInt(33)));
        }

        if (themeContains(theme, "farmer")) {
            extras.add(new ItemStack(Material.GOLDEN_CARROT, 16 + rng.nextInt(17)));
            extras.add(new ItemStack(Material.HAY_BLOCK, 8 + rng.nextInt(9)));
            extras.add(new ItemStack(Material.BONE_MEAL, 16));
        }

        if (themeContains(theme, "lava", "nether", "pyro", "inferno", "phoenix", "blaze", "dragon")) {
            extras.add(new ItemStack(Material.FIRE_CHARGE, 6 + rng.nextInt(11)));
            extras.add(splash(PotionType.FIRE_RESISTANCE, false));
            if (rng.nextDouble() < 0.45) extras.add(new ItemStack(Material.LAVA_BUCKET));
            extras.add(new ItemStack(Material.NETHERRACK, 24 + rng.nextInt(25)));
        }

        if (themeContains(theme, "ice", "snow", "frozen", "cryo")) {
            extras.add(new ItemStack(Material.PACKED_ICE, 16 + rng.nextInt(17)));
            extras.add(new ItemStack(Material.SNOWBALL, 12 + rng.nextInt(21)));
            if (rng.nextDouble() < 0.4) extras.add(splash(PotionType.SLOWNESS, true));
        }

        if (themeContains(theme, "mage", "magic", "potion", "mystic", "crystal", "thunder", "storm")) {
            extras.add(splash(PotionType.STRENGTH, rng.nextBoolean()));
            extras.add(splash(PotionType.SWIFTNESS, false));
            extras.add(splash(PotionType.HEALING, true));
            if (rng.nextDouble() < 0.5) extras.add(splash(PotionType.POISON, true));
            extras.add(new ItemStack(Material.ENDER_PEARL, 1 + rng.nextInt(3)));
        }

        if (themeContains(theme, "bomber", "tnt", "trap", "chaos")) {
            extras.add(new ItemStack(Material.TNT, 2 + rng.nextInt(5)));
            extras.add(new ItemStack(Material.FLINT_AND_STEEL));
            if (rng.nextDouble() < 0.5) extras.add(new ItemStack(Material.COBWEB, 2 + rng.nextInt(5)));
            if (rng.nextDouble() < 0.4) extras.add(new ItemStack(Material.OBSIDIAN, 4 + rng.nextInt(5)));
        }

        if (themeContains(theme, "end", "void", "galaxy", "astral")) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 4 + rng.nextInt(5)));
            extras.add(new ItemStack(Material.CHORUS_FRUIT, 8 + rng.nextInt(9)));
            extras.add(new ItemStack(Material.END_STONE, 16 + rng.nextInt(17)));
        }

        if (themeContains(theme, "pirate", "ocean", "diver")) {
            extras.add(new ItemStack(Material.TRIDENT));
            extras.add(new ItemStack(Material.WATER_BUCKET));
            extras.add(new ItemStack(Material.COOKED_COD, 12 + rng.nextInt(9)));
            if (rng.nextDouble() < 0.5) extras.add(new ItemStack(Material.FISHING_ROD));
        }

        if (themeContains(theme, "samurai", "gladiator", "champion", "berserker")) {
            extras.add(new ItemStack(Material.GOLDEN_APPLE, 1 + rng.nextInt(3)));
            if (rng.nextDouble() < 0.4) extras.add(splash(PotionType.STRENGTH, false));
        }

        if (themeContains(theme, "explorer", "scout", "nomad", "jungle", "desert", "mountain", "forest")) {
            extras.add(new ItemStack(Material.TORCH, 24 + rng.nextInt(25)));
            extras.add(new ItemStack(Material.BREAD, 16));
            extras.add(new ItemStack(Material.OAK_BOAT));
        }
    }

    private void addRandomUtilities(List<ItemStack> extras, GearBand band, String theme,
                                    FileConfiguration cfg, ThreadLocalRandom rng) {
        // Food — always
        int food = range(cfg, "generation.food", 16, 40, rng);
        extras.add(new ItemStack(FOODS[rng.nextInt(FOODS.length)], Math.min(64, food)));

        // Blocks — almost always
        if (rng.nextDouble() < cfg.getDouble("generation.blocks-chance", 0.9)) {
            int blocks = range(cfg, "generation.blocks", 24, 64, rng);
            extras.add(new ItemStack(BLOCKS[rng.nextInt(BLOCKS.length)], Math.min(64, blocks)));
        }

        // Golden apples
        if (rng.nextDouble() < cfg.getDouble("generation.golden-apple-chance", band == GearBand.IRON ? 0.45 : 0.7)) {
            extras.add(new ItemStack(Material.GOLDEN_APPLE, 1 + rng.nextInt(band == GearBand.NETHERITE_PARTIAL ? 4 : 3)));
        }

        // Water / lava buckets
        if (rng.nextDouble() < cfg.getDouble("generation.water-bucket-chance", 0.55)) {
            extras.add(new ItemStack(Material.WATER_BUCKET));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.lava-bucket-chance", 0.22)) {
            extras.add(new ItemStack(Material.LAVA_BUCKET));
        }

        // Pearls
        if (rng.nextDouble() < cfg.getDouble("generation.ender-pearl-chance", 0.4)) {
            extras.add(new ItemStack(Material.ENDER_PEARL, 1 + rng.nextInt(4)));
        }

        // Cobwebs / obsidian / tnt / fire charges
        if (rng.nextDouble() < cfg.getDouble("generation.cobweb-chance", 0.28)) {
            extras.add(new ItemStack(Material.COBWEB, 1 + rng.nextInt(4)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.obsidian-chance", 0.22)) {
            extras.add(new ItemStack(Material.OBSIDIAN, 2 + rng.nextInt(7)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.tnt-chance", 0.18)) {
            extras.add(new ItemStack(Material.TNT, 1 + rng.nextInt(3)));
            if (rng.nextBoolean()) extras.add(new ItemStack(Material.FLINT_AND_STEEL));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.fire-charge-chance", 0.25)) {
            extras.add(new ItemStack(Material.FIRE_CHARGE, 4 + rng.nextInt(9)));
        }

        // Fishing rod / snowballs / eggs / torches / shield chance
        if (rng.nextDouble() < cfg.getDouble("generation.fishing-rod-chance", 0.15)) {
            extras.add(new ItemStack(Material.FISHING_ROD));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.snowball-chance", 0.2)) {
            extras.add(new ItemStack(Material.SNOWBALL, 8 + rng.nextInt(17)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.egg-chance", 0.15)) {
            extras.add(new ItemStack(Material.EGG, 8 + rng.nextInt(9)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.torch-chance", 0.55)) {
            extras.add(new ItemStack(Material.TORCH, 16 + rng.nextInt(33)));
        }
        if (rng.nextDouble() < cfg.getDouble("generation.shield-chance", 0.35)) {
            boolean hasShield = extras.stream().anyMatch(i -> i.getType() == Material.SHIELD);
            if (!hasShield) extras.add(new ItemStack(Material.SHIELD));
        }

        // Bow chance for non-archer kits
        if (!themeContains(theme, "archer", "sniper", "hunter")
                && rng.nextDouble() < cfg.getDouble("generation.bow-chance", 0.3)) {
            ItemStack bow = new ItemStack(rng.nextBoolean() ? Material.BOW : Material.CROSSBOW);
            if (rng.nextDouble() < 0.4) {
                bow.addUnsafeEnchantment(bow.getType() == Material.BOW ? Enchantment.POWER : Enchantment.QUICK_CHARGE, 1);
            }
            extras.add(bow);
            extras.add(new ItemStack(Material.ARROW, range(cfg, "generation.arrows", 16, 48, rng)));
        }

        // Potion chance
        if (rng.nextDouble() < cfg.getDouble("generation.potion-chance", 0.4)) {
            PotionType[] types = {PotionType.STRENGTH, PotionType.SWIFTNESS, PotionType.HEALING, PotionType.FIRE_RESISTANCE};
            extras.add(splash(types[rng.nextInt(types.length)], rng.nextBoolean()));
        }

        // Tools for utility themes / random
        if (rng.nextDouble() < cfg.getDouble("generation.tool-chance", 0.25)) {
            Material tool = switch (rng.nextInt(3)) {
                case 0 -> band == GearBand.IRON ? Material.IRON_PICKAXE : Material.DIAMOND_PICKAXE;
                case 1 -> band == GearBand.IRON ? Material.IRON_SHOVEL : Material.DIAMOND_SHOVEL;
                default -> band == GearBand.IRON ? Material.IRON_AXE : Material.DIAMOND_AXE;
            };
            ItemStack stack = new ItemStack(tool);
            if (rng.nextDouble() < 0.4) stack.addUnsafeEnchantment(Enchantment.EFFICIENCY, 1 + rng.nextInt(2));
            extras.add(stack);
        }
    }

    private void maybeEnchantArmor(ItemStack[] armor, FileConfiguration cfg, ThreadLocalRandom rng) {
        if (rng.nextDouble() > cfg.getDouble("generation.enchant-chance", 0.45)) return;
        int level = 1 + rng.nextInt(2); // Prot I-II only
        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;
            piece.addUnsafeEnchantment(Enchantment.PROTECTION, level);
            if (rng.nextDouble() < 0.35) {
                piece.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(2));
            }
        }
    }

    private void maybeEnchantWeapon(ItemStack weapon, FileConfiguration cfg, ThreadLocalRandom rng) {
        if (weapon == null || weapon.getType().isAir()) return;
        if (rng.nextDouble() > cfg.getDouble("generation.enchant-chance", 0.45)) return;
        if (weapon.getType() == Material.BOW || weapon.getType() == Material.CROSSBOW || weapon.getType() == Material.TRIDENT) {
            return; // already handled for specialty weapons
        }
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 1 + rng.nextInt(2)); // I-II
        if (rng.nextDouble() < 0.25) weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        if (rng.nextDouble() < 0.2) weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        if (rng.nextDouble() < 0.4) weapon.addUnsafeEnchantment(Enchantment.UNBREAKING, 1 + rng.nextInt(2));
    }

    private ItemStack splash(PotionType type, boolean upgraded) {
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
