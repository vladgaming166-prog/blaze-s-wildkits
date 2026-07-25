package com.blaze.wildkits.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerData {

    private final UUID uuid;
    private String name;
    private long coins;
    private int kills;
    private int deaths;
    private int killstreak;
    private int bestKillstreak;
    private int level = 1;
    private int xp;
    private String currentKit;
    private String activeTrail;
    private String activeDeathEffect;
    private String activeVictoryEffect;
    private String activeTag;
    private String activeTitle;
    private String activeKillEffect;
    private String activePrefix;
    private final Set<String> unlockedKits = new LinkedHashSet<>();
    private final Set<String> unlockedCosmetics = new LinkedHashSet<>();
    private final Set<String> favorites = new LinkedHashSet<>();
    private final List<String> recentKits = new ArrayList<>();
    private final Map<String, Integer> crateKeys = new HashMap<>();
    private final Map<String, Integer> questProgress = new HashMap<>();
    private final Set<String> questCompleted = new LinkedHashSet<>();
    private long lastDaily;
    private long lastDailyQuestReset;
    private long lastWeeklyQuestReset;
    private long playtimeSeconds;
    private boolean dirty;
    private boolean protectedSpawn;
    private long protectionUntil;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; markDirty(); }

    public long getCoins() { return coins; }
    public void setCoins(long coins) { this.coins = Math.max(0, coins); markDirty(); }
    public void addCoins(long amount) { setCoins(this.coins + amount); }
    public boolean takeCoins(long amount) {
        if (coins < amount) return false;
        setCoins(coins - amount);
        return true;
    }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = Math.max(0, kills); markDirty(); }
    public void addKill() {
        this.kills++;
        this.killstreak++;
        if (killstreak > bestKillstreak) bestKillstreak = killstreak;
        addXp(15);
        markDirty();
    }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = Math.max(0, deaths); markDirty(); }
    public void addDeath() {
        this.deaths++;
        this.killstreak = 0;
        markDirty();
    }

    public int getKillstreak() { return killstreak; }
    public void setKillstreak(int killstreak) { this.killstreak = Math.max(0, killstreak); markDirty(); }
    public int getBestKillstreak() { return bestKillstreak; }
    public void setBestKillstreak(int bestKillstreak) { this.bestKillstreak = Math.max(0, bestKillstreak); markDirty(); }

    public double getKd() {
        if (deaths <= 0) return kills;
        return Math.round((kills / (double) deaths) * 100.0) / 100.0;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); markDirty(); }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = Math.max(0, xp); markDirty(); }

    public void addXp(int amount) {
        this.xp += Math.max(0, amount);
        while (this.xp >= xpToLevel(level)) {
            this.xp -= xpToLevel(level);
            this.level++;
        }
        markDirty();
    }

    public static int xpToLevel(int level) {
        return 100 + (level - 1) * 50;
    }

    public String getCurrentKit() { return currentKit; }
    public void setCurrentKit(String currentKit) { this.currentKit = currentKit; markDirty(); }

    public String getActiveTrail() { return activeTrail; }
    public void setActiveTrail(String activeTrail) { this.activeTrail = activeTrail; markDirty(); }
    public String getActiveDeathEffect() { return activeDeathEffect; }
    public void setActiveDeathEffect(String activeDeathEffect) { this.activeDeathEffect = activeDeathEffect; markDirty(); }
    public String getActiveVictoryEffect() { return activeVictoryEffect; }
    public void setActiveVictoryEffect(String activeVictoryEffect) { this.activeVictoryEffect = activeVictoryEffect; markDirty(); }
    public String getActiveTag() { return activeTag; }
    public void setActiveTag(String activeTag) { this.activeTag = activeTag; markDirty(); }
    public String getActiveTitle() { return activeTitle; }
    public void setActiveTitle(String activeTitle) { this.activeTitle = activeTitle; markDirty(); }
    public String getActiveKillEffect() { return activeKillEffect; }
    public void setActiveKillEffect(String activeKillEffect) { this.activeKillEffect = activeKillEffect; markDirty(); }
    public String getActivePrefix() { return activePrefix; }
    public void setActivePrefix(String activePrefix) { this.activePrefix = activePrefix; markDirty(); }

    public void addCrateKeys(String rarity, int amount) {
        if (rarity == null || amount == 0) return;
        String key = rarity.toLowerCase(Locale.ROOT);
        crateKeys.put(key, Math.max(0, crateKeys.getOrDefault(key, 0) + amount));
        markDirty();
    }

    public boolean takeCrateKey(String rarity, int amount) {
        if (rarity == null || amount <= 0) return false;
        String key = rarity.toLowerCase(Locale.ROOT);
        int have = crateKeys.getOrDefault(key, 0);
        if (have < amount) return false;
        crateKeys.put(key, have - amount);
        markDirty();
        return true;
    }

    public int getCrateKeys(String rarity) {
        return rarity == null ? 0 : crateKeys.getOrDefault(rarity.toLowerCase(Locale.ROOT), 0);
    }

    public Map<String, Integer> getCrateKeysMap() { return Collections.unmodifiableMap(crateKeys); }
    public void setCrateKeysMap(Map<String, Integer> map) {
        crateKeys.clear();
        if (map != null) crateKeys.putAll(map);
        markDirty();
    }

    public int getQuestProgress(String key) {
        return questProgress.getOrDefault(key, 0);
    }

    public void setQuestProgress(String key, int value) {
        if (key == null) return;
        questProgress.put(key, Math.max(0, value));
        markDirty();
    }

    public boolean isQuestCompleted(String key) {
        return key != null && questCompleted.contains(key);
    }

    public void setQuestCompleted(String key, boolean completed) {
        if (key == null) return;
        if (completed) questCompleted.add(key);
        else questCompleted.remove(key);
        markDirty();
    }

    public Map<String, Integer> getQuestProgressMap() { return Collections.unmodifiableMap(questProgress); }
    public void setQuestProgressMap(Map<String, Integer> map) {
        questProgress.clear();
        if (map != null) questProgress.putAll(map);
        markDirty();
    }

    public Set<String> getQuestCompletedSet() { return Collections.unmodifiableSet(questCompleted); }
    public void setQuestCompletedSet(Set<String> set) {
        questCompleted.clear();
        if (set != null) questCompleted.addAll(set);
        markDirty();
    }

    public long getLastDailyQuestReset() { return lastDailyQuestReset; }
    public void setLastDailyQuestReset(long lastDailyQuestReset) { this.lastDailyQuestReset = lastDailyQuestReset; markDirty(); }
    public long getLastWeeklyQuestReset() { return lastWeeklyQuestReset; }
    public void setLastWeeklyQuestReset(long lastWeeklyQuestReset) { this.lastWeeklyQuestReset = lastWeeklyQuestReset; markDirty(); }

    public boolean hasUnlockedKit(String id) {
        return id != null && unlockedKits.contains(id.toLowerCase(Locale.ROOT));
    }

    public void unlockKit(String id) {
        if (id != null) {
            unlockedKits.add(id.toLowerCase(Locale.ROOT));
            markDirty();
        }
    }

    public Set<String> getUnlockedKits() { return Collections.unmodifiableSet(unlockedKits); }
    public void setUnlockedKits(Set<String> kits) {
        unlockedKits.clear();
        if (kits != null) unlockedKits.addAll(kits);
        markDirty();
    }

    public boolean hasCosmetic(String id) {
        return id != null && unlockedCosmetics.contains(id.toLowerCase(Locale.ROOT));
    }

    public void unlockCosmetic(String id) {
        if (id != null) {
            unlockedCosmetics.add(id.toLowerCase(Locale.ROOT));
            markDirty();
        }
    }

    public Set<String> getUnlockedCosmetics() { return Collections.unmodifiableSet(unlockedCosmetics); }
    public void setUnlockedCosmetics(Set<String> cosmetics) {
        unlockedCosmetics.clear();
        if (cosmetics != null) unlockedCosmetics.addAll(cosmetics);
        markDirty();
    }

    public boolean isFavorite(String kitId) {
        return kitId != null && favorites.contains(kitId.toLowerCase(Locale.ROOT));
    }

    public void toggleFavorite(String kitId) {
        if (kitId == null) return;
        String id = kitId.toLowerCase(Locale.ROOT);
        if (!favorites.remove(id)) {
            favorites.add(id);
        }
        markDirty();
    }

    public Set<String> getFavorites() { return Collections.unmodifiableSet(favorites); }
    public void setFavorites(Set<String> favoritesIn) {
        favorites.clear();
        if (favoritesIn != null) favorites.addAll(favoritesIn);
        markDirty();
    }

    public void addRecentKit(String kitId) {
        if (kitId == null) return;
        String id = kitId.toLowerCase(Locale.ROOT);
        recentKits.remove(id);
        recentKits.add(0, id);
        while (recentKits.size() > 24) {
            recentKits.remove(recentKits.size() - 1);
        }
        markDirty();
    }

    public List<String> getRecentKits() { return Collections.unmodifiableList(recentKits); }
    public void setRecentKits(List<String> recent) {
        recentKits.clear();
        if (recent != null) recentKits.addAll(recent);
        markDirty();
    }

    public long getLastDaily() { return lastDaily; }
    public void setLastDaily(long lastDaily) { this.lastDaily = lastDaily; markDirty(); }

    public long getPlaytimeSeconds() { return playtimeSeconds; }
    public void setPlaytimeSeconds(long playtimeSeconds) { this.playtimeSeconds = Math.max(0, playtimeSeconds); markDirty(); }
    public void addPlaytimeSeconds(long seconds) { setPlaytimeSeconds(playtimeSeconds + seconds); }

    public boolean isProtectedSpawn() {
        return protectedSpawn && System.currentTimeMillis() < protectionUntil;
    }

    public void setProtection(long durationMillis) {
        this.protectedSpawn = durationMillis > 0;
        this.protectionUntil = System.currentTimeMillis() + durationMillis;
    }

    public void clearProtection() {
        this.protectedSpawn = false;
        this.protectionUntil = 0;
    }

    public boolean isDirty() { return dirty; }
    public void markDirty() { this.dirty = true; }
    public void clearDirty() { this.dirty = false; }
}
