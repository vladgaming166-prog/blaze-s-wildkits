package com.blaze.wildkits.player;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class PlayerDataManager {

    private final BlazesWildKits plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public PlayerData get(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), id -> new PlayerData(id, player.getName()));
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public void loadAsync(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        plugin.getDatabaseManager().executeAsync(connection -> {
            PlayerData data = loadFromDb(connection, uuid, name);
            Bukkit.getScheduler().runTask(plugin, () -> {
                cache.put(uuid, data);
                if (player.isOnline()) {
                    plugin.getScoreboardManager().show(player);
                }
            });
        });
    }

    private PlayerData loadFromDb(Connection connection, UUID uuid, String name) throws SQLException {
        String sql = "SELECT * FROM wildkits_players WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlayerData data = new PlayerData(uuid, name);
                    data.setCoins(rs.getLong("coins"));
                    data.setKills(rs.getInt("kills"));
                    data.setDeaths(rs.getInt("deaths"));
                    data.setKillstreak(rs.getInt("killstreak"));
                    data.setBestKillstreak(rs.getInt("best_killstreak"));
                    data.setLevel(rs.getInt("level"));
                    data.setXp(rs.getInt("xp"));
                    data.setCurrentKit(rs.getString("current_kit"));
                    data.setActiveTrail(rs.getString("active_trail"));
                    data.setActiveDeathEffect(rs.getString("active_death_effect"));
                    data.setActiveVictoryEffect(rs.getString("active_victory_effect"));
                    data.setActiveTag(rs.getString("active_tag"));
                    data.setActiveTitle(rs.getString("active_title"));
                    data.setUnlockedKits(splitSet(rs.getString("unlocked_kits")));
                    data.setUnlockedCosmetics(splitSet(rs.getString("unlocked_cosmetics")));
                    data.setFavorites(splitSet(rs.getString("favorites")));
                    data.setRecentKits(splitList(rs.getString("recent_kits")));
                    data.setLastDaily(rs.getLong("last_daily"));
                    data.setPlaytimeSeconds(rs.getLong("playtime_seconds"));
                    data.clearDirty();
                    return data;
                }
            }
        }
        PlayerData fresh = new PlayerData(uuid, name);
        insert(connection, fresh);
        fresh.clearDirty();
        return fresh;
    }

    private void insert(Connection connection, PlayerData data) throws SQLException {
        String sql = """
                INSERT INTO wildkits_players(uuid, name, coins, kills, deaths, killstreak, best_killstreak,
                level, xp, current_kit, active_trail, active_death_effect, active_victory_effect,
                active_tag, active_title, unlocked_kits, unlocked_cosmetics, favorites, recent_kits,
                last_daily, playtime_seconds) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, data);
            ps.executeUpdate();
        }
    }

    public void saveAsync(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null || !data.isDirty()) return;
        PlayerData snapshot = copy(data);
        data.clearDirty();
        plugin.getDatabaseManager().executeAsync(connection -> upsert(connection, snapshot));
    }

    public void saveAllSync() {
        for (PlayerData data : cache.values()) {
            if (!data.isDirty()) continue;
            try (Connection connection = plugin.getDatabaseManager().getConnection()) {
                upsert(connection, data);
                data.clearDirty();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed saving " + data.getUuid(), e);
            }
        }
    }

    private void upsert(Connection connection, PlayerData data) throws SQLException {
        String sql = """
                INSERT INTO wildkits_players(uuid, name, coins, kills, deaths, killstreak, best_killstreak,
                level, xp, current_kit, active_trail, active_death_effect, active_victory_effect,
                active_tag, active_title, unlocked_kits, unlocked_cosmetics, favorites, recent_kits,
                last_daily, playtime_seconds) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT(uuid) DO UPDATE SET
                name=excluded.name, coins=excluded.coins, kills=excluded.kills, deaths=excluded.deaths,
                killstreak=excluded.killstreak, best_killstreak=excluded.best_killstreak, level=excluded.level,
                xp=excluded.xp, current_kit=excluded.current_kit, active_trail=excluded.active_trail,
                active_death_effect=excluded.active_death_effect, active_victory_effect=excluded.active_victory_effect,
                active_tag=excluded.active_tag, active_title=excluded.active_title,
                unlocked_kits=excluded.unlocked_kits, unlocked_cosmetics=excluded.unlocked_cosmetics,
                favorites=excluded.favorites, recent_kits=excluded.recent_kits, last_daily=excluded.last_daily,
                playtime_seconds=excluded.playtime_seconds
                """;
        // MySQL uses different upsert syntax
        if ("mysql".equalsIgnoreCase(plugin.getDatabaseManager().getType())) {
            sql = """
                    INSERT INTO wildkits_players(uuid, name, coins, kills, deaths, killstreak, best_killstreak,
                    level, xp, current_kit, active_trail, active_death_effect, active_victory_effect,
                    active_tag, active_title, unlocked_kits, unlocked_cosmetics, favorites, recent_kits,
                    last_daily, playtime_seconds) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    ON DUPLICATE KEY UPDATE
                    name=VALUES(name), coins=VALUES(coins), kills=VALUES(kills), deaths=VALUES(deaths),
                    killstreak=VALUES(killstreak), best_killstreak=VALUES(best_killstreak), level=VALUES(level),
                    xp=VALUES(xp), current_kit=VALUES(current_kit), active_trail=VALUES(active_trail),
                    active_death_effect=VALUES(active_death_effect), active_victory_effect=VALUES(active_victory_effect),
                    active_tag=VALUES(active_tag), active_title=VALUES(active_title),
                    unlocked_kits=VALUES(unlocked_kits), unlocked_cosmetics=VALUES(unlocked_cosmetics),
                    favorites=VALUES(favorites), recent_kits=VALUES(recent_kits), last_daily=VALUES(last_daily),
                    playtime_seconds=VALUES(playtime_seconds)
                    """;
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, data);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, PlayerData data) throws SQLException {
        ps.setString(1, data.getUuid().toString());
        ps.setString(2, data.getName());
        ps.setLong(3, data.getCoins());
        ps.setInt(4, data.getKills());
        ps.setInt(5, data.getDeaths());
        ps.setInt(6, data.getKillstreak());
        ps.setInt(7, data.getBestKillstreak());
        ps.setInt(8, data.getLevel());
        ps.setInt(9, data.getXp());
        ps.setString(10, data.getCurrentKit());
        ps.setString(11, data.getActiveTrail());
        ps.setString(12, data.getActiveDeathEffect());
        ps.setString(13, data.getActiveVictoryEffect());
        ps.setString(14, data.getActiveTag());
        ps.setString(15, data.getActiveTitle());
        ps.setString(16, join(data.getUnlockedKits()));
        ps.setString(17, join(data.getUnlockedCosmetics()));
        ps.setString(18, join(data.getFavorites()));
        ps.setString(19, String.join(",", data.getRecentKits()));
        ps.setLong(20, data.getLastDaily());
        ps.setLong(21, data.getPlaytimeSeconds());
    }

    private PlayerData copy(PlayerData src) {
        PlayerData data = new PlayerData(src.getUuid(), src.getName());
        data.setCoins(src.getCoins());
        data.setKills(src.getKills());
        data.setDeaths(src.getDeaths());
        data.setKillstreak(src.getKillstreak());
        data.setBestKillstreak(src.getBestKillstreak());
        data.setLevel(src.getLevel());
        data.setXp(src.getXp());
        data.setCurrentKit(src.getCurrentKit());
        data.setActiveTrail(src.getActiveTrail());
        data.setActiveDeathEffect(src.getActiveDeathEffect());
        data.setActiveVictoryEffect(src.getActiveVictoryEffect());
        data.setActiveTag(src.getActiveTag());
        data.setActiveTitle(src.getActiveTitle());
        data.setUnlockedKits(new LinkedHashSet<>(src.getUnlockedKits()));
        data.setUnlockedCosmetics(new LinkedHashSet<>(src.getUnlockedCosmetics()));
        data.setFavorites(new LinkedHashSet<>(src.getFavorites()));
        data.setRecentKits(List.copyOf(src.getRecentKits()));
        data.setLastDaily(src.getLastDaily());
        data.setPlaytimeSeconds(src.getPlaytimeSeconds());
        return data;
    }

    public void unload(UUID uuid) {
        saveAsync(uuid);
        Bukkit.getScheduler().runTaskLater(plugin, () -> cache.remove(uuid), 40L);
    }

    private static Set<String> splitSet(String raw) {
        if (raw == null || raw.isBlank()) return new LinkedHashSet<>();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<String> splitList(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private static String join(Set<String> set) {
        return String.join(",", set);
    }
}
