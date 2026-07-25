package com.blaze.wildkits.kit;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Ships 100+ premium WildKits templates.
 * Administrators do not need to create kits from scratch.
 */
public final class DefaultKits {

    private DefaultKits() {}

    public static List<KitDefinition> createAll() {
        List<KitDefinition> kits = new ArrayList<>();
        kits.add(kit("starter", "Iron Recruit", "Reliable iron opener", KitRarity.COMMON, Material.IRON_SWORD, "wildkits.kit.starter", "General", true, 0, "balanced", false, false));
        kits.add(kit("warrior", "Diamond Warrior", "Diamond melee pressure", KitRarity.COMMON, Material.DIAMOND_SWORD, "wildkits.kit.warrior", "Melee", true, 0, "balanced", false, false));
        kits.add(kit("knight", "Shield Knight", "Frontline shield fighter", KitRarity.UNCOMMON, Material.SHIELD, "wildkits.kit.knight", "Melee", true, 0, "knight", false, false));
        kits.add(kit("tank", "Iron Tank", "Heavy defense kit", KitRarity.UNCOMMON, Material.IRON_CHESTPLATE, "wildkits.kit.tank", "Melee", true, 0, "tank", false, false));
        kits.add(kit("assassin", "Pearl Assassin", "Pearls and burst damage", KitRarity.RARE, Material.ENDER_PEARL, "wildkits.kit.assassin", "Stealth", false, 500, "assassin", false, false));
        kits.add(kit("archer", "Power Archer", "Bow dominance", KitRarity.COMMON, Material.BOW, "wildkits.kit.archer", "Ranged", true, 0, "archer", true, false));
        kits.add(kit("mage", "Potion Mage", "Splash control kit", KitRarity.RARE, Material.SPLASH_POTION, "wildkits.kit.mage", "Magic", false, 750, "mage", false, true));
        kits.add(kit("miner", "Diamond Miner", "Pickaxe and torches", KitRarity.COMMON, Material.DIAMOND_PICKAXE, "wildkits.kit.miner", "Utility", true, 0, "miner", false, false));
        kits.add(kit("builder", "Bridge Builder", "Blocks and scaffolding", KitRarity.COMMON, Material.SCAFFOLDING, "wildkits.kit.builder", "Utility", true, 0, "builder", false, false));
        kits.add(kit("hunter", "Crossbow Hunter", "Crossbow pursuit kit", KitRarity.UNCOMMON, Material.CROSSBOW, "wildkits.kit.hunter", "Ranged", true, 0, "hunter", true, false));
        kits.add(kit("farmer", "Golden Farmer", "Food surplus kit", KitRarity.COMMON, Material.GOLDEN_CARROT, "wildkits.kit.farmer", "Utility", true, 0, "farmer", false, false));
        kits.add(kit("explorer", "Trail Explorer", "Travel utility kit", KitRarity.COMMON, Material.COMPASS, "wildkits.kit.explorer", "Utility", true, 0, "explorer", false, false));
        kits.add(kit("pvp", "PvP Striker", "Classic diamond PvP", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "wildkits.kit.pvp", "Melee", true, 0, "balanced", false, false));
        kits.add(kit("nether", "Nether Raider", "Fireproof nether assault", KitRarity.RARE, Material.NETHERRACK, "wildkits.kit.nether", "Elemental", false, 800, "nether", false, false));
        kits.add(kit("end", "End Fighter", "Pearls and chorus", KitRarity.RARE, Material.ENDER_PEARL, "wildkits.kit.end", "Elemental", false, 900, "end", false, false));
        kits.add(kit("diamond", "Diamond Assault", "Full diamond pressure", KitRarity.EPIC, Material.DIAMOND_CHESTPLATE, "wildkits.kit.diamond", "Elite", false, 1500, "balanced", false, false));
        kits.add(kit("netherite", "Partial Netherite", "Rare netherite fragments", KitRarity.LEGENDARY, Material.NETHERITE_SWORD, "wildkits.kit.netherite", "Elite", false, 3000, "balanced", false, false));
        kits.add(kit("lucky", "Lucky Roll", "High variance loot", KitRarity.RARE, Material.GOLD_INGOT, "wildkits.kit.lucky", "Special", false, 1000, "chaos", false, false));
        kits.add(kit("op", "Chaos Overload", "Showcase chaos kit", KitRarity.ULTIMATE, Material.NETHER_STAR, "wildkits.kit.op", "Elite", false, 10000, "chaos", false, false));
        kits.add(kit("legend", "Legend Blade", "Legendary presence", KitRarity.LEGENDARY, Material.GOLDEN_HELMET, "wildkits.kit.legend", "Elite", false, 4000, "champion", false, false));
        kits.add(kit("mythic", "Mythic Edge", "Mythic-grade arms", KitRarity.MYTHIC, Material.DRAGON_EGG, "wildkits.kit.mythic", "Elite", false, 6000, "mystic", false, false));
        kits.add(kit("champion", "Arena Champion", "Tournament kit", KitRarity.EPIC, Material.DIAMOND_HELMET, "wildkits.kit.champion", "Elite", false, 2000, "champion", false, false));
        kits.add(kit("elite", "Elite Guard", "Elite standard", KitRarity.EPIC, Material.DIAMOND_BOOTS, "wildkits.kit.elite", "Elite", false, 1800, "knight", false, false));
        kits.add(kit("lava", "Lava Bomber", "Lava and fire charges", KitRarity.RARE, Material.LAVA_BUCKET, "wildkits.kit.lava", "Elemental", false, 850, "lava", false, false));
        kits.add(kit("ice", "Ice Mage", "Frost control", KitRarity.RARE, Material.PACKED_ICE, "wildkits.kit.ice", "Elemental", false, 850, "ice", false, true));
        kits.add(kit("storm", "Thunder Storm", "Trident tempest", KitRarity.EPIC, Material.TRIDENT, "wildkits.kit.storm", "Elemental", false, 1600, "thunder", false, true));
        kits.add(kit("speed", "Speed Runner", "Swift mobility", KitRarity.UNCOMMON, Material.SUGAR, "wildkits.kit.speed", "Mobility", true, 0, "speed", false, false));
        kits.add(kit("sniper", "Longbow Sniper", "Precision arrows", KitRarity.RARE, Material.ARROW, "wildkits.kit.sniper", "Ranged", false, 700, "sniper", true, false));
        kits.add(kit("samurai", "Samurai Blade", "Blade discipline", KitRarity.EPIC, Material.DIAMOND_SWORD, "wildkits.kit.samurai", "Melee", false, 1700, "samurai", false, false));
        kits.add(kit("pirate", "Ocean Pirate", "Trident sea kit", KitRarity.UNCOMMON, Material.TRIDENT, "wildkits.kit.pirate", "Special", true, 0, "pirate", false, false));
        kits.add(kit("ninja", "Shadow Ninja", "Silent lethal kit", KitRarity.RARE, Material.BLACK_DYE, "wildkits.kit.ninja", "Stealth", false, 900, "ninja", false, false));
        kits.add(kit("viking", "Viking Raider", "Axe fury", KitRarity.UNCOMMON, Material.DIAMOND_AXE, "wildkits.kit.viking", "Melee", true, 0, "viking", false, false));
        kits.add(kit("gladiator", "Gladiator", "Arena brawler", KitRarity.UNCOMMON, Material.IRON_CHESTPLATE, "wildkits.kit.gladiator", "Melee", true, 0, "gladiator", false, false));
        kits.add(kit("scout", "Scout Runner", "Light recon kit", KitRarity.COMMON, Material.LEATHER_BOOTS, "wildkits.kit.scout", "Mobility", true, 0, "scout", false, false));
        kits.add(kit("berserker", "Berserker", "Aggressive axe rush", KitRarity.RARE, Material.DIAMOND_AXE, "wildkits.kit.berserker", "Melee", false, 650, "berserker", false, false));
        kits.add(kit("paladin", "Guardian Paladin", "Holy defender", KitRarity.EPIC, Material.GOLDEN_APPLE, "wildkits.kit.paladin", "Melee", false, 1900, "guardian", false, false));
        kits.add(kit("rogue", "Rogue Duelist", "Shadowy duelist", KitRarity.RARE, Material.ENDER_EYE, "wildkits.kit.rogue", "Stealth", false, 800, "assassin", false, false));
        kits.add(kit("ranger", "Wild Ranger", "Wilderness marksman", KitRarity.UNCOMMON, Material.BOW, "wildkits.kit.ranger", "Ranged", true, 0, "hunter", true, false));
        kits.add(kit("warlock", "Warlock", "Dark magic kit", KitRarity.EPIC, Material.SOUL_LANTERN, "wildkits.kit.warlock", "Magic", false, 2100, "mage", false, true));
        kits.add(kit("necromancer", "Necromancer", "Soul mage", KitRarity.LEGENDARY, Material.WITHER_SKELETON_SKULL, "wildkits.kit.necromancer", "Magic", false, 4500, "mage", false, true));
        kits.add(kit("druid", "Forest Druid", "Nature kit", KitRarity.RARE, Material.OAK_SAPLING, "wildkits.kit.druid", "Magic", false, 950, "forest", false, true));
        kits.add(kit("phoenix", "Phoenix", "Ash rebirth kit", KitRarity.MYTHIC, Material.BLAZE_POWDER, "wildkits.kit.phoenix", "Elemental", false, 7000, "phoenix", false, false));
        kits.add(kit("frostbite", "Frozen Bite", "Deep freeze", KitRarity.EPIC, Material.BLUE_ICE, "wildkits.kit.frostbite", "Elemental", false, 1750, "frozen", false, false));
        kits.add(kit("thunder", "Thunder Strike", "Lightning kit", KitRarity.EPIC, Material.LIGHTNING_ROD, "wildkits.kit.thunder", "Elemental", false, 1800, "thunder", false, true));
        kits.add(kit("shadow", "Shadow", "Umbral assassin", KitRarity.LEGENDARY, Material.BLACK_CONCRETE, "wildkits.kit.shadow", "Stealth", false, 4200, "shadow", false, false));
        kits.add(kit("crimson", "Crimson Raider", "Crimson nether", KitRarity.RARE, Material.CRIMSON_NYLIUM, "wildkits.kit.crimson", "Elemental", false, 1000, "nether", false, false));
        kits.add(kit("warden", "Deep Warden", "Deep dark terror", KitRarity.MYTHIC, Material.SCULK_SHRIEKER, "wildkits.kit.warden", "Elite", false, 8000, "tank", false, false));
        kits.add(kit("guardian", "Ocean Guardian", "Protective ocean", KitRarity.UNCOMMON, Material.PRISMARINE_SHARD, "wildkits.kit.guardian", "Special", true, 0, "ocean", false, false));
        kits.add(kit("diver", "Deep Diver", "Underwater fighter", KitRarity.COMMON, Material.HEART_OF_THE_SEA, "wildkits.kit.diver", "Special", true, 0, "ocean", false, false));
        kits.add(kit("bomber", "TNT Expert", "Explosive pressure", KitRarity.RARE, Material.TNT, "wildkits.kit.bomber", "Special", false, 1100, "bomber", false, false));
        kits.add(kit("cannoneer", "Fire Cannoneer", "Heavy projectiles", KitRarity.EPIC, Material.FIRE_CHARGE, "wildkits.kit.cannoneer", "Ranged", false, 2000, "sniper", true, false));
        kits.add(kit("marksman", "Marksman", "Steady aim", KitRarity.UNCOMMON, Material.CROSSBOW, "wildkits.kit.marksman", "Ranged", true, 0, "archer", true, false));
        kits.add(kit("duelist", "Duelist", "1v1 specialist", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "wildkits.kit.duelist", "Melee", true, 0, "balanced", false, false));
        kits.add(kit("brawler", "Axe Brawler", "Close-quarters axe", KitRarity.COMMON, Material.DIAMOND_AXE, "wildkits.kit.brawler", "Melee", true, 0, "viking", false, false));
        kits.add(kit("sentinel", "Shield Sentinel", "Watchful defender", KitRarity.RARE, Material.SHIELD, "wildkits.kit.sentinel", "Melee", false, 900, "tank", false, false));
        kits.add(kit("templar", "Royal Templar", "Sacred knight", KitRarity.LEGENDARY, Material.GOLDEN_CHESTPLATE, "wildkits.kit.templar", "Melee", false, 4800, "royal", false, false));
        kits.add(kit("reaper", "Shadow Reaper", "Harvest souls", KitRarity.MYTHIC, Material.NETHERITE_HOE, "wildkits.kit.reaper", "Stealth", false, 7500, "shadow", false, false));
        kits.add(kit("ghost", "Ghost", "Hard to pin", KitRarity.EPIC, Material.GHAST_TEAR, "wildkits.kit.ghost", "Stealth", false, 2200, "ninja", false, false));
        kits.add(kit("vampire", "Vampire", "Life-steal vibes", KitRarity.LEGENDARY, Material.REDSTONE_BLOCK, "wildkits.kit.vampire", "Stealth", false, 5000, "assassin", false, false));
        kits.add(kit("werewolf", "Werewolf", "Feral hunter", KitRarity.EPIC, Material.BONE, "wildkits.kit.werewolf", "Melee", false, 2300, "hunter", true, false));
        kits.add(kit("alchemist", "Potion Master", "Potion specialist", KitRarity.RARE, Material.BREWING_STAND, "wildkits.kit.alchemist", "Magic", false, 1200, "potion", false, true));
        kits.add(kit("enchanter", "Enchanter", "Enchant-heavy", KitRarity.EPIC, Material.ENCHANTING_TABLE, "wildkits.kit.enchanter", "Magic", false, 2400, "magic", false, true));
        kits.add(kit("sorcerer", "Crystal Sorcerer", "Arcane barrage", KitRarity.LEGENDARY, Material.AMETHYST_SHARD, "wildkits.kit.sorcerer", "Magic", false, 4600, "crystal", false, true));
        kits.add(kit("pyromancer", "Pyromancer", "Fire magic", KitRarity.EPIC, Material.BLAZE_ROD, "wildkits.kit.pyromancer", "Magic", false, 2500, "pyro", false, true));
        kits.add(kit("cryomancer", "Cryomancer", "Ice magic", KitRarity.EPIC, Material.SNOWBALL, "wildkits.kit.cryomancer", "Magic", false, 2500, "ice", false, true));
        kits.add(kit("electromancer", "Stormcaller", "Shock magic", KitRarity.LEGENDARY, Material.COPPER_INGOT, "wildkits.kit.electromancer", "Magic", false, 4700, "storm", false, true));
        kits.add(kit("geomancer", "Mountain Mage", "Earth shaper", KitRarity.RARE, Material.DIRT, "wildkits.kit.geomancer", "Magic", false, 1300, "mountain", false, true));
        kits.add(kit("aeromancer", "Wind Runner", "Wind dancer", KitRarity.RARE, Material.FEATHER, "wildkits.kit.aeromancer", "Magic", false, 1300, "speed", false, true));
        kits.add(kit("engineer", "Trap Master", "Utility gadgets", KitRarity.UNCOMMON, Material.TRIPWIRE_HOOK, "wildkits.kit.engineer", "Utility", true, 0, "trap", false, false));
        kits.add(kit("architect", "Fortress Architect", "Master builder", KitRarity.RARE, Material.BRICKS, "wildkits.kit.architect", "Utility", false, 1000, "fortress", false, false));
        kits.add(kit("quarry", "Quarry Miner", "Heavy mining", KitRarity.UNCOMMON, Material.DIAMOND_PICKAXE, "wildkits.kit.quarry", "Utility", true, 0, "miner", false, false));
        kits.add(kit("lumberjack", "Lumberjack", "Axe and wood", KitRarity.COMMON, Material.DIAMOND_AXE, "wildkits.kit.lumberjack", "Utility", true, 0, "lumberjack", false, false));
        kits.add(kit("fisherman", "Fisherman", "Rod and ocean", KitRarity.COMMON, Material.FISHING_ROD, "wildkits.kit.fisherman", "Utility", true, 0, "pirate", false, false));
        kits.add(kit("chef", "Chef", "Food abundance", KitRarity.COMMON, Material.COOKED_BEEF, "wildkits.kit.chef", "Utility", true, 0, "farmer", false, false));
        kits.add(kit("beekeeper", "Beekeeper", "Sweet survival", KitRarity.UNCOMMON, Material.HONEYCOMB, "wildkits.kit.beekeeper", "Utility", true, 0, "farmer", false, false));
        kits.add(kit("herbalist", "Herbalist", "Natural remedies", KitRarity.UNCOMMON, Material.GLISTERING_MELON_SLICE, "wildkits.kit.herbalist", "Utility", true, 0, "farmer", false, false));
        kits.add(kit("nomad", "Desert Nomad", "Travel light", KitRarity.COMMON, Material.MAP, "wildkits.kit.nomad", "Mobility", true, 0, "desert", false, false));
        kits.add(kit("courier", "Courier", "Speed delivery", KitRarity.UNCOMMON, Material.RABBIT_FOOT, "wildkits.kit.courier", "Mobility", true, 0, "speed", false, false));
        kits.add(kit("parkour", "Parkourist", "Mobility specialist", KitRarity.RARE, Material.SLIME_BLOCK, "wildkits.kit.parkour", "Mobility", false, 900, "speed", false, false));
        kits.add(kit("skirmisher", "Skirmisher", "Hit and run", KitRarity.UNCOMMON, Material.LEATHER_CHESTPLATE, "wildkits.kit.skirmisher", "Mobility", true, 0, "assassin", false, false));
        kits.add(kit("raider", "Raider", "Aggressive loot", KitRarity.RARE, Material.CROSSBOW, "wildkits.kit.raider", "Melee", false, 1100, "viking", false, false));
        kits.add(kit("bandit", "Bandit", "Dirty fighter", KitRarity.COMMON, Material.IRON_SWORD, "wildkits.kit.bandit", "Stealth", true, 0, "assassin", false, false));
        kits.add(kit("mercenary", "Mercenary", "Paid killer", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "wildkits.kit.mercenary", "Melee", true, 0, "balanced", false, false));
        kits.add(kit("captain", "Captain", "Commanding presence", KitRarity.EPIC, Material.GOLDEN_HELMET, "wildkits.kit.captain", "Special", false, 2600, "pirate", false, false));
        kits.add(kit("admiral", "Admiral", "Naval supremacy", KitRarity.LEGENDARY, Material.HEART_OF_THE_SEA, "wildkits.kit.admiral", "Special", false, 4900, "ocean", false, false));
        kits.add(kit("corsair", "Corsair", "Swift sea raider", KitRarity.RARE, Material.NAUTILUS_SHELL, "wildkits.kit.corsair", "Special", false, 1400, "pirate", false, false));
        kits.add(kit("musketeer", "Musketeer", "Classic shot", KitRarity.UNCOMMON, Material.CROSSBOW, "wildkits.kit.musketeer", "Ranged", true, 0, "archer", true, false));
        kits.add(kit("longbow", "Longbow", "Heavy bow", KitRarity.RARE, Material.BOW, "wildkits.kit.longbow", "Ranged", false, 1000, "sniper", true, false));
        kits.add(kit("crossbowman", "Crossbowman", "Piercing bolts", KitRarity.UNCOMMON, Material.CROSSBOW, "wildkits.kit.crossbowman", "Ranged", true, 0, "archer", true, false));
        kits.add(kit("trapper", "Trap Master", "Field control", KitRarity.RARE, Material.COBWEB, "wildkits.kit.trapper", "Special", false, 1150, "trap", true, false));
        kits.add(kit("beastmaster", "Beastmaster", "Hunt ferocity", KitRarity.EPIC, Material.BONE, "wildkits.kit.beastmaster", "Ranged", false, 2100, "hunter", true, false));
        kits.add(kit("falconer", "Falconer", "Aerial hunter", KitRarity.RARE, Material.FEATHER, "wildkits.kit.falconer", "Ranged", false, 1200, "hunter", true, false));
        kits.add(kit("desert", "Desert Stalker", "Sandstorm survivor", KitRarity.COMMON, Material.SANDSTONE, "wildkits.kit.desert", "Elemental", true, 0, "desert", false, false));
        kits.add(kit("jungle", "Jungle Scout", "Dense foliage", KitRarity.COMMON, Material.VINE, "wildkits.kit.jungle", "Elemental", true, 0, "jungle", false, false));
        kits.add(kit("tundra", "Tundra", "Frozen wastes", KitRarity.UNCOMMON, Material.SNOW_BLOCK, "wildkits.kit.tundra", "Elemental", true, 0, "frozen", false, false));
        kits.add(kit("volcano", "Inferno", "Eruption offense", KitRarity.LEGENDARY, Material.MAGMA_BLOCK, "wildkits.kit.volcano", "Elemental", false, 5100, "inferno", false, false));
        kits.add(kit("ocean", "Ocean", "Tidal warrior", KitRarity.UNCOMMON, Material.PRISMARINE, "wildkits.kit.ocean", "Elemental", true, 0, "ocean", false, false));
        kits.add(kit("swamp", "Swamp", "Murky ambusher", KitRarity.COMMON, Material.LILY_PAD, "wildkits.kit.swamp", "Stealth", true, 0, "assassin", false, false));
        kits.add(kit("cave", "Cave Miner", "Underground fighter", KitRarity.COMMON, Material.COBBLESTONE, "wildkits.kit.cave", "Utility", true, 0, "miner", false, false));
        kits.add(kit("sky", "Sky Runner", "High ground", KitRarity.RARE, Material.ELYTRA, "wildkits.kit.sky", "Mobility", false, 1500, "speed", false, false));
        kits.add(kit("void", "Void Walker", "Void-touched", KitRarity.MYTHIC, Material.END_CRYSTAL, "wildkits.kit.void", "Elite", false, 8500, "void", false, true));
        kits.add(kit("astral", "Galaxy Astral", "Starlight magic", KitRarity.LEGENDARY, Material.END_ROD, "wildkits.kit.astral", "Magic", false, 5200, "galaxy", false, true));
        kits.add(kit("cosmic", "Galaxy Core", "Universe kit", KitRarity.ULTIMATE, Material.NETHER_STAR, "wildkits.kit.cosmic", "Elite", false, 12000, "galaxy", false, true));
        kits.add(kit("royal", "Royal Guard", "Royal fighter", KitRarity.EPIC, Material.GOLDEN_SWORD, "wildkits.kit.royal", "Elite", false, 2800, "royal", false, false));
        kits.add(kit("imperial", "Imperial", "Empire elite", KitRarity.LEGENDARY, Material.DIAMOND_BLOCK, "wildkits.kit.imperial", "Elite", false, 5500, "royal", false, false));
        kits.add(kit("emperor", "Emperor", "Absolute authority", KitRarity.ULTIMATE, Material.GOLD_BLOCK, "wildkits.kit.emperor", "Elite", false, 15000, "royal", false, false));
        kits.add(kit("noble", "Noble", "Aristocrat fighter", KitRarity.RARE, Material.GOLD_INGOT, "wildkits.kit.noble", "Elite", false, 1600, "royal", false, false));
        kits.add(kit("squire", "Squire", "Training knight", KitRarity.COMMON, Material.IRON_SWORD, "wildkits.kit.squire", "Melee", true, 0, "knight", false, false));
        kits.add(kit("cavalier", "Cavalier", "Charge kit", KitRarity.UNCOMMON, Material.SADDLE, "wildkits.kit.cavalier", "Melee", true, 0, "knight", false, false));
        kits.add(kit("dragoon", "Dragoon", "Shock troop", KitRarity.EPIC, Material.IRON_HORSE_ARMOR, "wildkits.kit.dragoon", "Melee", false, 2700, "balanced", false, false));
        kits.add(kit("centurion", "Centurion", "Legion commander", KitRarity.EPIC, Material.IRON_CHESTPLATE, "wildkits.kit.centurion", "Melee", false, 2900, "tank", false, false));
        kits.add(kit("spartan", "Spartan", "Shield spirit", KitRarity.RARE, Material.SHIELD, "wildkits.kit.spartan", "Melee", false, 1450, "tank", false, false));
        kits.add(kit("amazon", "Amazon", "Fierce warrior", KitRarity.RARE, Material.BOW, "wildkits.kit.amazon", "Ranged", false, 1450, "archer", true, false));
        kits.add(kit("valkyrie", "Valkyrie", "Winged champion", KitRarity.LEGENDARY, Material.ELYTRA, "wildkits.kit.valkyrie", "Elite", false, 5600, "speed", false, false));
        kits.add(kit("olympus", "Olympus", "Godlike showcase", KitRarity.ULTIMATE, Material.TOTEM_OF_UNDYING, "wildkits.kit.olympus", "Elite", false, 20000, "mystic", false, true));
        kits.add(kit("titan", "Titan", "Colossal strength", KitRarity.MYTHIC, Material.ANVIL, "wildkits.kit.titan", "Melee", false, 9000, "tank", false, false));
        kits.add(kit("colossus", "Colossus", "Unstoppable", KitRarity.MYTHIC, Material.NETHERITE_CHESTPLATE, "wildkits.kit.colossus", "Melee", false, 9500, "tank", false, false));
        kits.add(kit("juggernaut", "Juggernaut", "Slow but deadly", KitRarity.LEGENDARY, Material.NETHERITE_HELMET, "wildkits.kit.juggernaut", "Melee", false, 5800, "tank", false, false));
        kits.add(kit("blitz", "Blitz", "Lightning rush", KitRarity.EPIC, Material.SUGAR, "wildkits.kit.blitz", "Mobility", false, 2300, "speed", false, false));
        kits.add(kit("flash", "Flash", "Pure speed", KitRarity.RARE, Material.RABBIT_FOOT, "wildkits.kit.flash", "Mobility", false, 1250, "speed", false, false));
        kits.add(kit("dash", "Dash", "Burst fighter", KitRarity.COMMON, Material.LEATHER_BOOTS, "wildkits.kit.dash", "Mobility", true, 0, "speed", false, false));
        kits.add(kit("stormcaller", "Storm Lord", "Weather war mage", KitRarity.LEGENDARY, Material.TRIDENT, "wildkits.kit.stormcaller", "Magic", false, 5400, "storm", false, true));
        kits.add(kit("tidehunter", "Tidehunter", "Oceanic predator", KitRarity.EPIC, Material.TRIDENT, "wildkits.kit.tidehunter", "Special", false, 2600, "ocean", false, false));
        kits.add(kit("sandstalker", "Sandstalker", "Desert ambush", KitRarity.RARE, Material.SAND, "wildkits.kit.sandstalker", "Stealth", false, 1350, "desert", false, false));
        kits.add(kit("nightblade", "Nightblade", "Moonlit killer", KitRarity.EPIC, Material.DIAMOND_SWORD, "wildkits.kit.nightblade", "Stealth", false, 2750, "ninja", false, false));
        kits.add(kit("dawnbreaker", "Dawnbreaker", "Radiant melee", KitRarity.LEGENDARY, Material.GOLDEN_SWORD, "wildkits.kit.dawnbreaker", "Melee", false, 5300, "knight", false, false));
        kits.add(kit("dusk", "Dusk", "Twilight skirmisher", KitRarity.RARE, Material.GRAY_DYE, "wildkits.kit.dusk", "Stealth", false, 1400, "assassin", false, false));
        kits.add(kit("solar", "Solar", "Sunfire warrior", KitRarity.EPIC, Material.SUNFLOWER, "wildkits.kit.solar", "Elemental", false, 2550, "lava", false, false));
        kits.add(kit("lunar", "Lunar", "Moonlit magic", KitRarity.EPIC, Material.END_STONE, "wildkits.kit.lunar", "Magic", false, 2550, "mage", false, true));
        kits.add(kit("stellar", "Stellar", "Star-forged", KitRarity.MYTHIC, Material.NETHER_STAR, "wildkits.kit.stellar", "Elite", false, 8800, "galaxy", false, true));
        kits.add(kit("obsidian", "Obsidian Wall", "Hardened defense", KitRarity.RARE, Material.OBSIDIAN, "wildkits.kit.obsidian", "Melee", false, 1500, "fortress", false, false));
        kits.add(kit("emerald", "Emerald", "Lucky green", KitRarity.EPIC, Material.EMERALD, "wildkits.kit.emerald", "Special", false, 3000, "lucky", false, false));
        kits.add(kit("ruby", "Ruby", "Crimson gem", KitRarity.EPIC, Material.REDSTONE, "wildkits.kit.ruby", "Special", false, 3000, "balanced", false, false));
        kits.add(kit("sapphire", "Sapphire", "Azure gem magic", KitRarity.EPIC, Material.LAPIS_LAZULI, "wildkits.kit.sapphire", "Magic", false, 3000, "mage", false, true));
        kits.add(kit("amethyst", "Amethyst", "Crystal mage", KitRarity.RARE, Material.AMETHYST_CLUSTER, "wildkits.kit.amethyst", "Magic", false, 1550, "crystal", false, true));
        kits.add(kit("copper", "Copper", "Oxidized fighter", KitRarity.COMMON, Material.COPPER_BLOCK, "wildkits.kit.copper", "General", true, 0, "balanced", false, false));
        kits.add(kit("ironclad", "Ironclad", "Solid iron", KitRarity.UNCOMMON, Material.IRON_BLOCK, "wildkits.kit.ironclad", "Melee", true, 0, "tank", false, false));
        kits.add(kit("gilded", "Gilded", "Golden flashy", KitRarity.RARE, Material.GOLD_BLOCK, "wildkits.kit.gilded", "Elite", false, 1700, "royal", false, false));
        kits.add(kit("prismatic", "Prismatic", "Rainbow rarity", KitRarity.MYTHIC, Material.TINTED_GLASS, "wildkits.kit.prismatic", "Special", false, 9200, "crystal", false, true));
        kits.add(kit("chaos", "Chaos", "Unstable RNG", KitRarity.LEGENDARY, Material.ENDER_EYE, "wildkits.kit.chaos", "Special", false, 6000, "chaos", false, false));
        kits.add(kit("order", "Order", "Perfectly balanced", KitRarity.EPIC, Material.COMPARATOR, "wildkits.kit.order", "Special", false, 2800, "balanced", false, false));
        kits.add(kit("fortune", "Fortune", "High luck feel", KitRarity.RARE, Material.RABBIT_FOOT, "wildkits.kit.fortune", "Special", false, 1600, "lucky", false, false));
        kits.add(kit("gambler", "Gambler", "All-in random", KitRarity.UNCOMMON, Material.GOLD_NUGGET, "wildkits.kit.gambler", "Special", true, 0, "lucky", false, false));
        kits.add(kit("wildcard", "Wildcard", "Anything happens", KitRarity.EPIC, Material.PLAYER_HEAD, "wildkits.kit.wildcard", "Special", false, 2500, "chaos", false, false));
        kits.add(kit("seasoned", "Seasoned", "Veteran loadout", KitRarity.UNCOMMON, Material.DIAMOND_SWORD, "wildkits.kit.seasoned", "General", true, 0, "balanced", false, false));
        kits.add(kit("recruit", "Recruit", "Training kit", KitRarity.COMMON, Material.IRON_SWORD, "wildkits.kit.recruit", "General", true, 0, "balanced", false, false));
        kits.add(kit("veteran", "Veteran", "Battle-tested", KitRarity.RARE, Material.SHIELD, "wildkits.kit.veteran", "General", false, 1200, "balanced", false, false));
        kits.add(kit("hero", "Hero", "Heroic comeback", KitRarity.LEGENDARY, Material.TOTEM_OF_UNDYING, "wildkits.kit.hero", "Elite", false, 5700, "champion", false, false));
        kits.add(kit("savior", "Savior", "Supportive fighter", KitRarity.EPIC, Material.GOLDEN_APPLE, "wildkits.kit.savior", "Special", false, 2400, "guardian", false, false));
        kits.add(kit("demon", "Demon", "Infernal menace", KitRarity.MYTHIC, Material.MAGMA_CREAM, "wildkits.kit.demon", "Elemental", false, 8600, "inferno", false, true));
        kits.add(kit("angel", "Angel", "Radiant protector", KitRarity.MYTHIC, Material.GLOWSTONE, "wildkits.kit.angel", "Elite", false, 8600, "guardian", false, true));
        kits.add(kit("dragon", "Dragon", "Dragonfire", KitRarity.ULTIMATE, Material.DRAGON_HEAD, "wildkits.kit.dragon", "Elite", false, 25000, "dragon", false, true));
        kits.add(kit("hydra", "Hydra", "Multi-threat", KitRarity.MYTHIC, Material.CREEPER_HEAD, "wildkits.kit.hydra", "Elite", false, 9100, "chaos", false, false));
        kits.add(kit("kraken", "Kraken", "Deep sea terror", KitRarity.LEGENDARY, Material.DARK_PRISMARINE, "wildkits.kit.kraken", "Special", false, 5900, "ocean", false, false));
        kits.add(kit("phoenixlord", "Phoenix Lord", "Rebirth incarnate", KitRarity.ULTIMATE, Material.BLAZE_POWDER, "wildkits.kit.phoenixlord", "Elemental", false, 22000, "phoenix", false, true));
        kits.add(kit("stormlord", "Storm Lord", "Tempest emperor", KitRarity.ULTIMATE, Material.TRIDENT, "wildkits.kit.stormlord", "Elemental", false, 22000, "storm", false, true));
        kits.add(kit("shadowlord", "Shadow Lord", "Absolute stealth", KitRarity.ULTIMATE, Material.BLACK_WOOL, "wildkits.kit.shadowlord", "Stealth", false, 22000, "shadow", false, false));
        kits.add(kit("kitmaster", "Kit Master", "Master of kits", KitRarity.ULTIMATE, Material.CHEST, "wildkits.kit.kitmaster", "Elite", false, 30000, "balanced", false, false));
        kits.add(kit("wild", "Wild", "True WildKits spirit", KitRarity.LEGENDARY, Material.FIREWORK_ROCKET, "wildkits.kit.wild", "Special", false, 6500, "chaos", false, false));
        kits.add(kit("blaze", "Blaze", "Signature Blaze kit", KitRarity.MYTHIC, Material.BLAZE_ROD, "wildkits.kit.blaze", "Elite", false, 10000, "blaze", false, true));
        kits.add(kit("diamond_axe", "Diamond Axe Only", "Diamond axe specialist", KitRarity.UNCOMMON, Material.DIAMOND_AXE, "wildkits.kit.diamond_axe", "Melee", true, 0, "viking", false, false));
        kits.add(kit("diamond_pick", "Diamond Pickaxe", "Mining pressure kit", KitRarity.COMMON, Material.DIAMOND_PICKAXE, "wildkits.kit.diamond_pick", "Utility", true, 0, "miner", false, false));
        kits.add(kit("trident_kit", "Trident Fighter", "Trident focused", KitRarity.RARE, Material.TRIDENT, "wildkits.kit.trident_kit", "Special", true, 0, "thunder", false, false));
        kits.add(kit("shield_kit", "Shield Wall", "Shield specialist", KitRarity.UNCOMMON, Material.SHIELD, "wildkits.kit.shield_kit", "Melee", true, 0, "tank", false, false));
        kits.add(kit("bow_kit", "Bow Kit", "Pure bow kit", KitRarity.COMMON, Material.BOW, "wildkits.kit.bow_kit", "Ranged", true, 0, "archer", true, false));
        kits.add(kit("crossbow_kit", "Crossbow Kit", "Pure crossbow", KitRarity.COMMON, Material.CROSSBOW, "wildkits.kit.crossbow_kit", "Ranged", true, 0, "archer", true, false));
        kits.add(kit("helmet_diamond_chest", "Helmet Split", "Helmet + diamond chest style", KitRarity.UNCOMMON, Material.DIAMOND_CHESTPLATE, "wildkits.kit.helmet_diamond_chest", "Melee", true, 0, "balanced", false, false));
        kits.add(kit("nether_sword_dia", "Nether Sword Diamond", "Netherite sword + diamond armor", KitRarity.EPIC, Material.NETHERITE_SWORD, "wildkits.kit.nether_sword_dia", "Elite", false, 2200, "balanced", false, false));
        kits.add(kit("pyromancer_blaze", "Blaze Pyromancer", "Fire charges and lava", KitRarity.RARE, Material.FIRE_CHARGE, "wildkits.kit.pyromancer_blaze", "Elemental", true, 0, "blaze", false, true));
        kits.add(kit("ancient", "Ancient", "Ancient ruins kit", KitRarity.EPIC, Material.COBBLED_DEEPSLATE, "wildkits.kit.ancient", "Special", false, 2100, "ancient", false, false));
        kits.add(kit("mystic", "Mystic", "Mystic energies", KitRarity.EPIC, Material.AMETHYST_SHARD, "wildkits.kit.mystic", "Magic", false, 2100, "mystic", false, true));
        kits.add(kit("inferno", "Inferno", "Pure fire kit", KitRarity.LEGENDARY, Material.MAGMA_BLOCK, "wildkits.kit.inferno", "Elemental", false, 5000, "inferno", false, false));
        kits.add(kit("forest", "Forest", "Forest survival", KitRarity.COMMON, Material.OAK_LEAVES, "wildkits.kit.forest", "Elemental", true, 0, "forest", false, false));
        kits.add(kit("mountain", "Mountain", "Peak climber", KitRarity.UNCOMMON, Material.STONE, "wildkits.kit.mountain", "Elemental", true, 0, "mountain", false, false));
        kits.add(kit("bridge", "Bridge Builder", "Scaffolds and planks", KitRarity.COMMON, Material.SCAFFOLDING, "wildkits.kit.bridge", "Utility", true, 0, "bridge", false, false));
        kits.add(kit("fortress", "Fortress", "Obsidian defense", KitRarity.RARE, Material.OBSIDIAN, "wildkits.kit.fortress", "Utility", false, 1300, "fortress", false, false));
        kits.add(kit("tnt_expert", "TNT Expert", "Explosive expert", KitRarity.RARE, Material.TNT, "wildkits.kit.tnt_expert", "Special", false, 1400, "tnt", false, false));
        kits.add(kit("trap_master", "Trap Master", "Cobwebs and control", KitRarity.RARE, Material.COBWEB, "wildkits.kit.trap_master", "Special", false, 1400, "trap", false, false));
        kits.add(kit("potion_master", "Potion Master", "Potion arsenal", KitRarity.RARE, Material.BREWING_STAND, "wildkits.kit.potion_master", "Magic", false, 1400, "potion", false, true));
        kits.add(kit("end_fighter", "End Fighter", "End dimension kit", KitRarity.RARE, Material.END_STONE, "wildkits.kit.end_fighter", "Elemental", true, 0, "end", false, false));
        kits.add(kit("nether_raider", "Nether Raider", "Nether assault", KitRarity.RARE, Material.NETHERRACK, "wildkits.kit.nether_raider", "Elemental", true, 0, "nether", false, false));
        kits.add(kit("ice_mage", "Ice Mage", "Ice magic specialist", KitRarity.RARE, Material.BLUE_ICE, "wildkits.kit.ice_mage", "Magic", true, 0, "ice", false, true));
        kits.add(kit("speed_runner", "Speed Runner", "Pure speed kit", KitRarity.UNCOMMON, Material.SUGAR, "wildkits.kit.speed_runner", "Mobility", true, 0, "speed", false, false));
        kits.add(kit("galaxy", "Galaxy", "Cosmic fighter", KitRarity.MYTHIC, Material.END_CRYSTAL, "wildkits.kit.galaxy", "Elite", false, 7000, "galaxy", false, true));
        kits.add(kit("crystal", "Crystal", "Crystal power", KitRarity.EPIC, Material.AMETHYST_BLOCK, "wildkits.kit.crystal", "Magic", false, 2000, "crystal", false, true));
        kits.add(kit("chaos_core", "Chaos Core", "Total chaos kit", KitRarity.LEGENDARY, Material.ENDER_EYE, "wildkits.kit.chaos_core", "Special", false, 5500, "chaos", false, false));
        kits.add(kit("legendary_blade", "Legendary", "Legendary template", KitRarity.LEGENDARY, Material.NETHERITE_SWORD, "wildkits.kit.legendary_blade", "Elite", false, 4500, "champion", false, false));
        kits.add(kit("mythic_core", "Mythic Core", "Mythic template", KitRarity.MYTHIC, Material.NETHER_STAR, "wildkits.kit.mythic_core", "Elite", false, 6500, "mystic", false, false));
        return kits;
    }

    private static KitDefinition kit(String id, String name, String desc, KitRarity rarity,
                                  Material icon, String permission, String category,
                                  boolean unlocked, int price, String theme,
                                  boolean ranged, boolean magic) {
        SmartGenProfile profile = SmartGenProfile.forRarity(rarity).withTheme(theme, ranged, magic);
        return new KitDefinition(id, "<gradient:#FF4500:#FFD700>" + name + "</gradient>", desc, rarity, icon, permission, category, unlocked, price,
                List.of(category.toLowerCase(), rarity.name().toLowerCase(), theme), profile);
    }
}
