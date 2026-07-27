package com.blaze.wildkits.scoreboard;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import com.blaze.wildkits.util.TextUtil;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chaos-style sidebar: MiniMessage gradients, shadows, animation placeholders,
 * and fully hidden vanilla score numbers.
 */
public final class ScoreboardManager {

    private static final String OBJECTIVE_NAME = "wildkits";
    private static final String[] ENTRIES = {
            "§0§r", "§1§r", "§2§r", "§3§r", "§4§r", "§5§r", "§6§r", "§7§r",
            "§8§r", "§9§r", "§a§r", "§b§r", "§c§r", "§d§r", "§e§r"
    };

    private final BlazesWildKits plugin;
    private final Map<UUID, BoardState> boards = new ConcurrentHashMap<>();
    private BukkitTask task;
    private List<String> cachedLines = List.of();
    private String titleTemplate = "<gradient:#FF4500:#FFD700><bold>Blaze's WildKits</bold></gradient>";
    private boolean showKitLine = true;

    public ScoreboardManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void start() {
        reload();
    }

    public void reload() {
        shutdown();
        var yaml = plugin.getConfigManager().getScoreboard();
        if (!yaml.getBoolean("enabled", true)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                hide(player);
            }
            return;
        }

        List<String> titles = yaml.getStringList("title-animation");
        if (!titles.isEmpty()) {
            // Prefer animations.yml %animation:title% when configured in lines/title
            titleTemplate = titles.get(0);
        } else {
            titleTemplate = yaml.getString("title",
                    "%animation:title%");
        }
        if (titleTemplate == null || titleTemplate.isBlank()) {
            titleTemplate = "%animation:title%";
        }

        cachedLines = yaml.getStringList("lines");
        if (cachedLines.isEmpty()) {
            cachedLines = defaultLines();
        }
        showKitLine = plugin.getConfigManager().isShowKit();

        long interval = Math.max(2L, yaml.getLong("update-interval-ticks", 10L));
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            plugin.getAnimationService().tick();
            showKitLine = plugin.getConfigManager().isShowKit();
            for (Player player : Bukkit.getOnlinePlayers()) {
                update(player);
            }
        }, 5L, interval);

        for (Player player : Bukkit.getOnlinePlayers()) {
            show(player);
        }
    }

    public void show(Player player) {
        if (!plugin.getConfigManager().getScoreboard().getBoolean("enabled", true)) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, Component.text("WildKits"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());

        BoardState state = new BoardState(board, objective);
        int lineCount = Math.min(ENTRIES.length, cachedLines.size());
        for (int i = 0; i < lineCount; i++) {
            Team team = board.registerNewTeam("wk" + i);
            team.addEntry(ENTRIES[i]);
            objective.getScore(ENTRIES[i]).setScore(lineCount - i);
            state.teams[i] = team;
            state.lastPrefixes[i] = null;
        }
        state.lineCount = lineCount;
        boards.put(player.getUniqueId(), state);
        player.setScoreboard(board);
        update(player);
    }

    public void hide(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void update(Player player) {
        BoardState state = boards.get(player.getUniqueId());
        if (state == null) {
            show(player);
            state = boards.get(player.getUniqueId());
            if (state == null) return;
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);
        String titleRaw = plugin.getAnimationService().apply(titleTemplate);
        // Allow rotating title-animation list as fallback
        List<String> titles = plugin.getConfigManager().getScoreboard().getStringList("title-animation");
        if (!titles.isEmpty() && !titleTemplate.contains("%animation:")) {
            int idx = Math.floorMod(plugin.getAnimationService().getAnimations().isEmpty()
                    ? 0
                    : (int) (System.currentTimeMillis() / 400), titles.size());
            titleRaw = plugin.getAnimationService().apply(titles.get(idx));
        }
        Component title = TextUtil.parse(ensureShadow(titleRaw));
        if (!title.equals(state.lastTitle)) {
            state.objective.displayName(title);
            state.lastTitle = title;
        }
        // Keep numbers hidden even after reloads/clients
        state.objective.numberFormat(NumberFormat.blank());

        int lineCount = Math.min(state.lineCount, cachedLines.size());
        for (int i = 0; i < lineCount; i++) {
            String line = cachedLines.get(i);
            if (!showKitLine && isKitLine(line)) {
                line = "shadow:black:1 <dark_gray>";
            }
            String parsed = applyPlaceholders(player, data, line);
            parsed = plugin.getAnimationService().apply(parsed);
            parsed = ensureShadow(parsed);
            if (parsed.equals(state.lastPrefixes[i])) {
                continue;
            }
            state.teams[i].prefix(TextUtil.parse(parsed));
            state.lastPrefixes[i] = parsed;
        }
    }

    private boolean isKitLine(String line) {
        if (line == null) return false;
        String lower = line.toLowerCase();
        return lower.contains("%currentkit%") || lower.contains("{currentkit}");
    }

    private String ensureShadow(String input) {
        if (input == null || input.isBlank()) return "shadow:black:1 <gray>";
        String trimmed = input.trim();
        if (trimmed.toLowerCase().startsWith("shadow:")) return trimmed;
        if (trimmed.contains("<shadow")) return trimmed;
        return "shadow:black:1 " + trimmed;
    }

    private String applyPlaceholders(Player player, PlayerData data, String line) {
        String kitDisplay;
        if (!plugin.getConfigManager().isShowKit()) {
            kitDisplay = plugin.getConfigManager().getConfig().getString("show-kit-hidden-text", "???");
        } else if (data.getCurrentKit() == null) {
            kitDisplay = "None";
        } else {
            kitDisplay = plugin.getKitManager().getKit(data.getCurrentKit())
                    .map(k -> TextUtil.strip(k.getDisplayName()))
                    .orElse(data.getCurrentKit());
        }

        String result = line
                .replace("%player%", player.getName())
                .replace("%coins%", String.valueOf(data.getCoins()))
                .replace("%kills%", String.valueOf(data.getKills()))
                .replace("%deaths%", String.valueOf(data.getDeaths()))
                .replace("%kd%", String.valueOf(data.getKd()))
                .replace("%killstreak%", String.valueOf(data.getKillstreak()))
                .replace("%level%", String.valueOf(data.getLevel()))
                .replace("%wins%", String.valueOf(data.getWins()))
                .replace("%rerolls%", String.valueOf(data.getKitRerolls()))
                .replace("%currentkit%", kitDisplay)
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%keys%", String.valueOf(
                        data.getCrateKeys("common") + data.getCrateKeys("rare") + data.getCrateKeys("epic")
                                + data.getCrateKeys("legendary") + data.getCrateKeys("mythic")));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
            } catch (Throwable ignored) {
            }
        }
        return result;
    }

    private List<String> defaultLines() {
        return List.of(
                "shadow:black:1 <dark_gray><st>----------------</st>",
                "shadow:black:1 <gray>Player <white>%player%",
                "shadow:black:1 <gray>Kit <gradient:#FF4500:#FFD700>%currentkit%</gradient>",
                "shadow:black:1 <gray>Coins <gold>%coins%",
                "shadow:black:1 <dark_gray>",
                "shadow:black:1 <gray>Kills <green>%kills%",
                "shadow:black:1 <gray>Deaths <red>%deaths%",
                "shadow:black:1 <gray>K/D <yellow>%kd%",
                "shadow:black:1 <gray>Streak <light_purple>%killstreak%",
                "shadow:black:1 <gray>Level <aqua>%level%",
                "shadow:black:1 <dark_gray><st>----------------</st>",
                "shadow:black:1 %animation:server%"
        );
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private static final class BoardState {
        private final Scoreboard board;
        private final Objective objective;
        private final Team[] teams = new Team[ENTRIES.length];
        private final String[] lastPrefixes = new String[ENTRIES.length];
        private Component lastTitle;
        private int lineCount;

        private BoardState(Scoreboard board, Objective objective) {
            this.board = board;
            this.objective = objective;
        }
    }
}
