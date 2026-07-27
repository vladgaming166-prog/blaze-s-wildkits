package com.blaze.wildkits.crate;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import com.blaze.wildkits.util.ItemBuilder;
import com.blaze.wildkits.util.TextUtil;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class CrateManager {

    private final BlazesWildKits plugin;
    private final Map<String, CrateDefinition> crates = new LinkedHashMap<>();
    private final Set<UUID> opening = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> lastReward = new ConcurrentHashMap<>();

    public CrateManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        crates.clear();
        File file = new File(plugin.getDataFolder(), "crates.yml");
        if (!file.exists()) {
            plugin.saveResource("crates.yml", false);
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("crates");
        if (section == null) {
            seedDefaults();
            save();
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection c = section.getConfigurationSection(id);
            if (c == null) continue;
            CrateDefinition def = new CrateDefinition(
                    id.toLowerCase(Locale.ROOT),
                    c.getString("display-name", id),
                    CrateRarity.from(c.getString("rarity", "COMMON")),
                    Material.matchMaterial(c.getString("block", "CHEST"))
            );
            if (c.isConfigurationSection("location")) {
                ConfigurationSection loc = c.getConfigurationSection("location");
                if (loc != null && Bukkit.getWorld(loc.getString("world", "world")) != null) {
                    def.setLocation(new Location(
                            Bukkit.getWorld(loc.getString("world", "world")),
                            loc.getDouble("x"), loc.getDouble("y"), loc.getDouble("z")
                    ));
                }
            }
            ConfigurationSection rewards = c.getConfigurationSection("rewards");
            if (rewards != null) {
                for (String rid : rewards.getKeys(false)) {
                    ConfigurationSection r = rewards.getConfigurationSection(rid);
                    if (r == null) continue;
                    def.addReward(new CrateDefinition.Reward(
                            rid,
                            r.getString("display-name", rid),
                            Material.matchMaterial(r.getString("icon", "GOLD_INGOT")),
                            r.getDouble("weight", 10),
                            r.getInt("coins", 0),
                            r.getInt("xp", 0),
                            r.getString("cosmetic-id", null),
                            r.getString("key-rarity", null),
                            r.getInt("keys", 0)
                    ));
                }
            }
            crates.put(def.getId(), def);
        }
        if (crates.isEmpty()) {
            seedDefaults();
            save();
        }
    }

    private void seedDefaults() {
        createDefault("common", "Common Crate", CrateRarity.COMMON, Material.CHEST);
        createDefault("rare", "Rare Crate", CrateRarity.RARE, Material.ENDER_CHEST);
        createDefault("epic", "Epic Crate", CrateRarity.EPIC, Material.TRAPPED_CHEST);
        createDefault("legendary", "Legendary Crate", CrateRarity.LEGENDARY, Material.GOLD_BLOCK);
        createDefault("mythic", "Mythic Crate", CrateRarity.MYTHIC, Material.NETHERITE_BLOCK);
    }

    private void createDefault(String id, String name, CrateRarity rarity, Material block) {
        CrateDefinition def = new CrateDefinition(id, "<gradient:#FF4500:#FFD700>" + name + "</gradient>", rarity, block);
        def.addReward(new CrateDefinition.Reward("coins_small", "Coin Pouch", Material.GOLD_NUGGET, 40, 100, 20, null, null, 0));
        def.addReward(new CrateDefinition.Reward("coins_med", "Coin Bag", Material.GOLD_INGOT, 25, 250, 40, null, null, 0));
        def.addReward(new CrateDefinition.Reward("trail_flame", "Flame Trail", Material.BLAZE_POWDER, 15, 0, 50, "flame", null, 0));
        def.addReward(new CrateDefinition.Reward("trail_heart", "Heart Trail", Material.RED_DYE, 10, 0, 50, "heart", null, 0));
        def.addReward(new CrateDefinition.Reward("key_upgrade", "Bonus Key", Material.TRIPWIRE_HOOK, 8, 0, 25, null, rarity.name().toLowerCase(Locale.ROOT), 1));
        def.addReward(new CrateDefinition.Reward("jackpot", "Jackpot", Material.NETHER_STAR, 2, 1000, 200, "elite", null, 0));
        crates.put(id, def);
    }

    public void save() {
        File file = new File(plugin.getDataFolder(), "crates.yml");
        FileConfiguration yaml = new YamlConfiguration();
        for (CrateDefinition def : crates.values()) {
            String path = "crates." + def.getId();
            yaml.set(path + ".display-name", def.getDisplayName());
            yaml.set(path + ".rarity", def.getRarity().name());
            yaml.set(path + ".block", def.getBlockType().name());
            if (def.getLocation() != null) {
                Location loc = def.getLocation();
                yaml.set(path + ".location.world", loc.getWorld().getName());
                yaml.set(path + ".location.x", loc.getX());
                yaml.set(path + ".location.y", loc.getY());
                yaml.set(path + ".location.z", loc.getZ());
            }
            int i = 0;
            for (CrateDefinition.Reward reward : def.getRewards()) {
                String rp = path + ".rewards.r" + (i++);
                yaml.set(rp + ".display-name", reward.displayName());
                yaml.set(rp + ".icon", reward.icon() == null ? "GOLD_INGOT" : reward.icon().name());
                yaml.set(rp + ".weight", reward.weight());
                yaml.set(rp + ".coins", reward.coins());
                yaml.set(rp + ".xp", reward.xp());
                yaml.set(rp + ".cosmetic-id", reward.cosmeticId());
                yaml.set(rp + ".key-rarity", reward.keyRarity());
                yaml.set(rp + ".keys", reward.keys());
            }
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed saving crates.yml: " + e.getMessage());
        }
    }

    public Collection<CrateDefinition> getCrates() {
        return Collections.unmodifiableCollection(crates.values());
    }

    public Optional<CrateDefinition> get(String id) {
        return Optional.ofNullable(crates.get(id.toLowerCase(Locale.ROOT)));
    }

    public CrateDefinition create(String id, CrateRarity rarity) {
        String key = id.toLowerCase(Locale.ROOT);
        CrateDefinition def = new CrateDefinition(key, "<gradient:#FF4500:#FFD700>" + id + "</gradient>", rarity, Material.CHEST);
        def.addReward(new CrateDefinition.Reward("coins", "Coins", Material.GOLD_INGOT, 50, 150, 25, null, null, 0));
        crates.put(key, def);
        save();
        return def;
    }

    public boolean delete(String id) {
        CrateDefinition removed = crates.remove(id.toLowerCase(Locale.ROOT));
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    public void setLocation(String id, Location location) {
        get(id).ifPresent(def -> {
            def.setLocation(location);
            Block block = location.getBlock();
            block.setType(def.getBlockType());
            save();
        });
    }

    public Optional<CrateDefinition> byLocation(Location location) {
        if (location == null) return Optional.empty();
        for (CrateDefinition def : crates.values()) {
            Location loc = def.getLocation();
            if (loc == null || loc.getWorld() == null) continue;
            if (loc.getWorld().equals(location.getWorld())
                    && loc.getBlockX() == location.getBlockX()
                    && loc.getBlockY() == location.getBlockY()
                    && loc.getBlockZ() == location.getBlockZ()) {
                return Optional.of(def);
            }
        }
        return Optional.empty();
    }

    public void giveKey(Player target, String rarity, int amount) {
        plugin.getPlayerDataManager().get(target).addCrateKeys(rarity.toLowerCase(Locale.ROOT), amount);
        plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
        plugin.getMessageService().send(target, "crate-key-received", Map.of(
                "rarity", rarity,
                "amount", String.valueOf(amount)
        ));
    }

    public boolean tryOpen(Player player, CrateDefinition crate) {
        if (opening.contains(player.getUniqueId())) return false;
        PlayerData data = plugin.getPlayerDataManager().get(player);
        String rarity = crate.getRarity().name().toLowerCase(Locale.ROOT);
        if (!data.takeCrateKey(rarity, 1) && !player.hasPermission("wildkits.admin")) {
            plugin.getMessageService().send(player, "crate-no-key", Map.of("rarity", rarity));
            return false;
        }
        opening.add(player.getUniqueId());
        animateOpen(player, crate);
        return true;
    }

    private void animateOpen(Player player, CrateDefinition crate) {
        Location loc = crate.getLocation() != null ? crate.getLocation().add(0.5, 1, 0.5) : player.getLocation().add(0, 1, 0);
        player.playSound(loc, Sound.BLOCK_CHEST_OPEN, 1f, 1f);
        for (int i = 0; i < 12; i++) {
            final int tick = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) {
                    opening.remove(player.getUniqueId());
                    return;
                }
                player.getWorld().spawnParticle(Particle.CRIT, loc, 12, 0.35, 0.4, 0.35, 0.02);
                player.getWorld().spawnParticle(Particle.FIREWORK, loc, 6, 0.2, 0.3, 0.2, 0.01);
                player.playSound(loc, Sound.UI_BUTTON_CLICK, 0.4f, 1.2f + tick * 0.05f);
                if (tick == 11) {
                    finishOpen(player, crate);
                }
            }, i * 3L);
        }
    }

    private void finishOpen(Player player, CrateDefinition crate) {
        CrateDefinition.Reward reward = rollReward(player, crate);
        applyReward(player, reward);
        plugin.getQuestManager().progress(player, "crate_open", 1);
        plugin.getPlayerDataManager().saveAsync(player.getUniqueId());

        Map<String, String> ph = Map.of(
                "player", player.getName(),
                "crate", TextUtil.strip(crate.getDisplayName()),
                "reward", reward.displayName()
        );
        player.showTitle(Title.title(
                TextUtil.parse("<gradient:#FF4500:#FFD700><bold>CRATE</bold></gradient>"),
                TextUtil.parse("<white>" + reward.displayName()),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1600), Duration.ofMillis(200))
        ));
        plugin.getMessageService().send(player, "crate-opened", ph);
        if (crate.getRarity() == CrateRarity.LEGENDARY || crate.getRarity() == CrateRarity.MYTHIC) {
            Bukkit.getServer().sendMessage(TextUtil.parse(
                    "<gold><bold>WildKits</bold></gold> <gray>»</gray> <white>"
                            + player.getName() + "</white> <gray>won</gray> <yellow>"
                            + reward.displayName() + "</yellow> <gray>from</gray> "
                            + crate.getDisplayName()
            ));
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.3f);
        opening.remove(player.getUniqueId());
    }

    private CrateDefinition.Reward rollReward(Player player, CrateDefinition crate) {
        List<CrateDefinition.Reward> rewards = new ArrayList<>(crate.getRewards());
        if (rewards.isEmpty()) {
            return new CrateDefinition.Reward("fallback", "Coins", Material.GOLD_INGOT, 1, 50, 10, null, null, 0);
        }
        String previous = lastReward.get(player.getUniqueId());
        double total = 0;
        double[] weights = new double[rewards.size()];
        for (int i = 0; i < rewards.size(); i++) {
            CrateDefinition.Reward r = rewards.get(i);
            double w = Math.max(0.01, r.weight());
            // Duplicate protection: heavily reduce chance of same reward twice
            if (previous != null && previous.equals(r.id())) {
                w *= 0.15;
            }
            total += w;
            weights[i] = total;
        }
        double roll = ThreadLocalRandom.current().nextDouble(total);
        for (int i = 0; i < rewards.size(); i++) {
            if (roll <= weights[i]) {
                lastReward.put(player.getUniqueId(), rewards.get(i).id());
                return rewards.get(i);
            }
        }
        CrateDefinition.Reward last = rewards.get(rewards.size() - 1);
        lastReward.put(player.getUniqueId(), last.id());
        return last;
    }

    private void applyReward(Player player, CrateDefinition.Reward reward) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (reward.coins() > 0) data.addCoins(reward.coins());
        if (reward.xp() > 0) data.addXp(reward.xp());
        if (reward.cosmeticId() != null && !reward.cosmeticId().isBlank()) {
            data.unlockCosmetic(reward.cosmeticId());
        }
        if (reward.keyRarity() != null && reward.keys() > 0) {
            data.addCrateKeys(reward.keyRarity(), reward.keys());
        }
    }

    public void openEditor(Player player, CrateDefinition crate) {
        Inventory inv = Bukkit.createInventory(player, 54, TextUtil.parse("<gold>Edit: " + crate.getDisplayName()));
        int slot = 10;
        for (CrateDefinition.Reward reward : crate.getRewards()) {
            if (slot >= 44) break;
            if (slot % 9 == 8) slot += 2;
            inv.setItem(slot++, new ItemBuilder(reward.icon() == null ? Material.CHEST : reward.icon())
                    .name(reward.displayName())
                    .lore(
                            "<gray>Weight: <white>" + reward.weight(),
                            "<gray>Coins: <gold>" + reward.coins(),
                            "<gray>XP: <aqua>" + reward.xp(),
                            reward.cosmeticId() == null ? "<dark_gray>No cosmetic" : "<gray>Cosmetic: <white>" + reward.cosmeticId()
                    ).build());
        }
        inv.setItem(49, new ItemBuilder(Material.TRIPWIRE_HOOK).name("<yellow>Keys: use /wk crate key").build());
        player.openInventory(inv);
    }
}
