package com.blaze.wildkits.scoreboard;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import com.blaze.wildkits.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ScoreboardManager {

    private final BlazesWildKits plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();
    private BukkitTask task;
    private int titleFrame;

    public ScoreboardManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void start() {
        reload();
    }

    public void reload() {
        shutdown();
        if (!plugin.getConfigManager().getScoreboard().getBoolean("enabled", true)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                hide(player);
            }
            return;
        }
        long interval = plugin.getConfigManager().getScoreboard().getLong("update-interval-ticks", 20L);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            titleFrame++;
            for (Player player : Bukkit.getOnlinePlayers()) {
                update(player);
            }
        }, 20L, Math.max(5L, interval));
        for (Player player : Bukkit.getOnlinePlayers()) {
            show(player);
        }
    }

    public void show(Player player) {
        if (!plugin.getConfigManager().getScoreboard().getBoolean("enabled", true)) return;
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("wildkits", Criteria.DUMMY, Component.text("WildKits"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        update(player);
    }

    public void hide(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void update(Player player) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) {
            show(player);
            board = boards.get(player.getUniqueId());
            if (board == null) return;
        }
        Objective objective = board.getObjective("wildkits");
        if (objective == null) return;

        List<String> titles = plugin.getConfigManager().getScoreboard().getStringList("title-animation");
        if (titles.isEmpty()) {
            titles = List.of("<gold><bold>WildKits</bold></gold>", "<yellow><bold>WildKits</bold></yellow>");
        }
        String title = titles.get(Math.floorMod(titleFrame, titles.size()));
        objective.displayName(TextUtil.parse(title));

        PlayerData data = plugin.getPlayerDataManager().get(player);
        List<String> lines = plugin.getConfigManager().getScoreboard().getStringList("lines");
        if (lines.isEmpty()) {
            lines = List.of(
                    "",
                    "<gray>Player <white>%player%",
                    "<gray>Kit <aqua>%currentkit%",
                    "<gray>Coins <gold>%coins%",
                    "<gray>Kills <green>%kills%",
                    "<gray>Deaths <red>%deaths%",
                    "<gray>K/D <yellow>%kd%",
                    "<gray>Streak <light_purple>%killstreak%",
                    "<gray>Level <aqua>%level%",
                    ""
            );
        }

        // Clear old teams/scores
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        for (Team team : board.getTeams()) {
            team.unregister();
        }

        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String parsed = applyPlaceholders(player, data, lines.get(i));
            String entry = colorEntry(i);
            Team team = board.registerNewTeam("wk_" + i);
            team.addEntry(entry);
            team.prefix(TextUtil.parse(parsed));
            objective.getScore(entry).setScore(score--);
        }
    }

    private String applyPlaceholders(Player player, PlayerData data, String line) {
        String currentKit = data.getCurrentKit() == null ? "None" : data.getCurrentKit();
        String result = line
                .replace("%player%", player.getName())
                .replace("%coins%", String.valueOf(data.getCoins()))
                .replace("%kills%", String.valueOf(data.getKills()))
                .replace("%deaths%", String.valueOf(data.getDeaths()))
                .replace("%kd%", String.valueOf(data.getKd()))
                .replace("%killstreak%", String.valueOf(data.getKillstreak()))
                .replace("%level%", String.valueOf(data.getLevel()))
                .replace("%currentkit%", currentKit)
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
            } catch (Throwable ignored) {
            }
        }
        return result;
    }

    private String colorEntry(int index) {
        // Unique invisible-ish entries so vanilla score numbers are irrelevant to content
        return "§" + Integer.toHexString(index % 16) + "§r";
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
