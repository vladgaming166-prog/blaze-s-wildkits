package com.blaze.wildkits.quest;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import com.blaze.wildkits.util.ItemBuilder;
import com.blaze.wildkits.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class QuestManager {

    private final BlazesWildKits plugin;
    private final Map<String, QuestDefinition> quests = new LinkedHashMap<>();
    private BukkitTask playtimeTask;

    public QuestManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        quests.clear();
        FileConfiguration yaml = plugin.getConfigManager().getQuests();
        ConfigurationSection section = yaml.getConfigurationSection("quests");
        if (section == null) {
            registerDefaults();
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection q = section.getConfigurationSection(id);
            if (q == null) continue;
            QuestType type = QuestType.valueOf(q.getString("type", "DAILY").toUpperCase(Locale.ROOT));
            Map<String, Integer> rewards = new LinkedHashMap<>();
            ConfigurationSection rewardSec = q.getConfigurationSection("rewards");
            if (rewardSec != null) {
                for (String key : rewardSec.getKeys(false)) {
                    rewards.put(key.toLowerCase(Locale.ROOT), rewardSec.getInt(key));
                }
            }
            quests.put(id.toLowerCase(Locale.ROOT), new QuestDefinition(
                    id.toLowerCase(Locale.ROOT),
                    q.getString("display-name", id),
                    q.getString("description", ""),
                    type,
                    q.getString("objective", "kill").toLowerCase(Locale.ROOT),
                    q.getInt("target", 1),
                    rewards
            ));
        }
        if (quests.isEmpty()) registerDefaults();
        startPlaytimeTracker();
    }

    private void registerDefaults() {
        add("daily_kills_5", "First Blood", "Kill 5 players", QuestType.DAILY, "kill", 5, Map.of("coins", 150, "xp", 50));
        add("daily_kills_20", "Hot Streak", "Kill 20 players", QuestType.DAILY, "kill", 20, Map.of("coins", 400, "crate_key_common", 1));
        add("daily_crates_5", "Lucky Hands", "Open 5 crates", QuestType.DAILY, "crate_open", 5, Map.of("coins", 200));
        add("daily_gaps", "Golden Appetite", "Use 10 golden apples", QuestType.DAILY, "golden_apple", 10, Map.of("coins", 175));
        add("daily_pearls", "Pearl Master", "Use 15 ender pearls", QuestType.DAILY, "ender_pearl", 15, Map.of("coins", 175));
        add("daily_play_30", "Warmup", "Play 30 minutes", QuestType.DAILY, "playtime_minutes", 30, Map.of("coins", 250, "xp", 100));
        add("daily_survive", "Survivor", "Survive 10 minutes without dying", QuestType.DAILY, "survive_minutes", 10, Map.of("coins", 300));
        add("weekly_kills_100", "Warlord", "Kill 100 players", QuestType.WEEKLY, "kill", 100, Map.of("coins", 2500, "crate_key_rare", 2, "xp", 500));
        add("weekly_crates_50", "Crate Grinder", "Open 50 crates", QuestType.WEEKLY, "crate_open", 50, Map.of("coins", 2000, "crate_key_epic", 1));
        add("weekly_fights", "Duelist", "Win 25 fights (kills)", QuestType.WEEKLY, "kill", 25, Map.of("coins", 1200));
        add("life_kills_100", "Centurion", "Kill 100 players lifetime", QuestType.LIFETIME, "kill", 100, Map.of("coins", 5000, "cosmetic_title_centurion", 1));
        add("life_legendary", "Legend Seeker", "Receive a Legendary kit", QuestType.LIFETIME, "legendary_kit", 1, Map.of("coins", 1000, "crate_key_legendary", 1));
        add("life_break_placed", "Demolisher", "Break 200 player-placed blocks", QuestType.LIFETIME, "break_placed", 200, Map.of("coins", 1500));
        add("life_damage", "Bruiser", "Deal 5000 damage", QuestType.LIFETIME, "deal_damage", 5000, Map.of("coins", 2000));
    }

    private void add(String id, String name, String desc, QuestType type, String objective, int target, Map<String, Integer> rewards) {
        quests.put(id, new QuestDefinition(id, name, desc, type, objective, target, rewards));
    }

    public void startPlaytimeTracker() {
        if (playtimeTask != null) playtimeTask.cancel();
        playtimeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                progress(player, "playtime_minutes", 1);
                PlayerData data = plugin.getPlayerDataManager().get(player);
                // survive minutes while killstreak active / alive - simple: every minute online in drop/lobby counts
                if (!player.isDead()) {
                    progress(player, "survive_minutes", 1);
                }
            }
        }, 20L * 60L, 20L * 60L);
    }

    public void shutdown() {
        if (playtimeTask != null) {
            playtimeTask.cancel();
            playtimeTask = null;
        }
    }

    public Collection<QuestDefinition> getQuests() {
        return Collections.unmodifiableCollection(quests.values());
    }

    public void progress(Player player, String objective, int amount) {
        if (player == null || amount <= 0) return;
        PlayerData data = plugin.getPlayerDataManager().get(player);
        resetIfNeeded(data);
        String obj = objective.toLowerCase(Locale.ROOT);
        boolean changed = false;
        for (QuestDefinition quest : quests.values()) {
            if (!quest.getObjective().equals(obj)) continue;
            String key = questKey(quest);
            if (data.isQuestCompleted(key)) continue;
            int current = data.getQuestProgress(key);
            int next = Math.min(quest.getTarget(), current + amount);
            if (next != current) {
                data.setQuestProgress(key, next);
                changed = true;
            }
            if (next >= quest.getTarget() && !data.isQuestCompleted(key)) {
                complete(player, data, quest);
                changed = true;
            }
        }
        if (changed) {
            plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
        }
    }

    private void complete(Player player, PlayerData data, QuestDefinition quest) {
        String key = questKey(quest);
        data.setQuestCompleted(key, true);
        grantRewards(player, data, quest);
        plugin.getMessageService().send(player, "quest-complete", Map.of(
                "quest", TextUtil.strip(quest.getDisplayName())
        ));
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
    }

    private void grantRewards(Player player, PlayerData data, QuestDefinition quest) {
        for (Map.Entry<String, Integer> entry : quest.getRewards().entrySet()) {
            String reward = entry.getKey();
            int amount = entry.getValue();
            if (reward.equals("coins")) {
                data.addCoins(amount);
            } else if (reward.equals("xp")) {
                data.addXp(amount);
            } else if (reward.startsWith("crate_key_")) {
                String rarity = reward.substring("crate_key_".length());
                data.addCrateKeys(rarity, amount);
            } else if (reward.startsWith("cosmetic_")) {
                String cosmeticId = reward.substring("cosmetic_".length());
                data.unlockCosmetic(cosmeticId);
            } else if (reward.startsWith("trail_")) {
                data.unlockCosmetic(reward.substring("trail_".length()));
            }
        }
    }

    private void resetIfNeeded(PlayerData data) {
        long now = System.currentTimeMillis();
        if (now - data.getLastDailyQuestReset() >= TimeUnit.DAYS.toMillis(1)) {
            clearType(data, QuestType.DAILY);
            data.setLastDailyQuestReset(now);
        }
        if (now - data.getLastWeeklyQuestReset() >= TimeUnit.DAYS.toMillis(7)) {
            clearType(data, QuestType.WEEKLY);
            data.setLastWeeklyQuestReset(now);
        }
    }

    private void clearType(PlayerData data, QuestType type) {
        for (QuestDefinition quest : quests.values()) {
            if (quest.getType() != type) continue;
            String key = questKey(quest);
            data.setQuestProgress(key, 0);
            data.setQuestCompleted(key, false);
        }
    }

    private String questKey(QuestDefinition quest) {
        return quest.getType().name().toLowerCase(Locale.ROOT) + ":" + quest.getId();
    }

    public void openGui(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        resetIfNeeded(data);
        Inventory inv = Bukkit.createInventory(player, 54, TextUtil.parse("<gold><bold>Quests</bold></gold>"));
        int slot = 10;
        for (QuestDefinition quest : quests.values()) {
            if (slot >= 44) break;
            if (slot % 9 == 8) slot += 2;
            String key = questKey(quest);
            int progress = data.getQuestProgress(key);
            boolean done = data.isQuestCompleted(key);
            List<String> lore = new ArrayList<>();
            lore.add("<gray>" + quest.getDescription());
            lore.add("");
            lore.add("<white>Type: <yellow>" + quest.getType().name());
            lore.add("<white>Progress: <aqua>" + progress + "/" + quest.getTarget());
            lore.add(done ? "<green>Completed" : "<yellow>In progress");
            lore.add("");
            lore.add("<gold>Rewards:");
            quest.getRewards().forEach((k, v) -> lore.add("<gray>- " + k + " <white>x" + v));
            inv.setItem(slot++, new ItemBuilder(done ? Material.LIME_DYE : Material.BOOK)
                    .name(quest.getDisplayName())
                    .lore(lore)
                    .build());
        }
        player.openInventory(inv);
    }
}
