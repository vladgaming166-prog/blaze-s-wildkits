package com.blaze.wildkits.config;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.kit.KitRarity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public final class ConfigManager {

    private final BlazesWildKits plugin;
    private FileConfiguration config;
    private FileConfiguration kits;
    private FileConfiguration shop;
    private FileConfiguration scoreboard;
    private FileConfiguration animations;
    private FileConfiguration database;
    private FileConfiguration particles;
    private FileConfiguration quests;

    public ConfigManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        this.kits = load("kits.yml");
        this.shop = load("shop.yml");
        this.scoreboard = load("scoreboard.yml");
        this.animations = load("animations.yml");
        this.database = load("database.yml");
        this.particles = load("particles.yml");
        this.quests = load("quests.yml");
    }

    private FileConfiguration load(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void save(FileConfiguration configuration, String name) {
        try {
            configuration.save(new File(plugin.getDataFolder(), name));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + name + ": " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getKits() { return kits; }
    public FileConfiguration getShop() { return shop; }
    public FileConfiguration getScoreboard() { return scoreboard; }
    public FileConfiguration getAnimations() { return animations; }
    public FileConfiguration getDatabase() { return database; }
    public FileConfiguration getParticles() { return particles; }
    public FileConfiguration getQuests() { return quests; }

    public boolean isAutoRandomKits() {
        return config.getBoolean("auto-random-kits", true);
    }

    public boolean isShowKit() {
        return config.getBoolean("show-kit", true);
    }

    public void setShowKit(boolean value) {
        config.set("show-kit", value);
        plugin.saveConfig();
    }

    public int getRespawnDelaySeconds() {
        return Math.max(0, config.getInt("respawn.delay-seconds", 0));
    }

    public int getRespawnProtectionSeconds() {
        return Math.max(0, config.getInt("respawn.protection-seconds", 3));
    }

    public boolean isRespawnHeal() { return config.getBoolean("respawn.heal", true); }
    public boolean isRespawnFeed() { return config.getBoolean("respawn.feed", true); }
    public boolean isRespawnClearEffects() { return config.getBoolean("respawn.clear-effects", true); }
    public boolean isRespawnTeleport() { return config.getBoolean("respawn.teleport-to-spawn", true); }

    public int getCoinsPerKill() { return config.getInt("economy.coins-per-kill", 25); }
    public int getCoinsPerKillstreakBonus() { return config.getInt("economy.coins-per-killstreak-bonus", 5); }
    public int getPlaytimeCoins() { return config.getInt("economy.playtime-coins", 10); }
    public int getPlaytimeIntervalMinutes() { return config.getInt("economy.playtime-interval-minutes", 5); }
    public int getDailyRewardCoins() { return config.getInt("economy.daily-reward-coins", 100); }

    public String getLanguage() {
        return config.getString("language", "en");
    }

    public Map<KitRarity, Double> getRarityWeights() {
        Map<KitRarity, Double> map = new EnumMap<>(KitRarity.class);
        for (KitRarity rarity : KitRarity.values()) {
            String path = "rarity-chances." + rarity.name().toLowerCase();
            map.put(rarity, config.getDouble(path, rarity.getDefaultWeight()));
        }
        return map;
    }
}
