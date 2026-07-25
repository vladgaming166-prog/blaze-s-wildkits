package com.blaze.wildkits.database;

import com.blaze.wildkits.BlazesWildKits;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public final class DatabaseManager {

    private final BlazesWildKits plugin;
    private HikariDataSource dataSource;
    private String type = "sqlite";

    public DatabaseManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        FileConfiguration db = plugin.getConfigManager().getDatabase();
        this.type = db.getString("type", "sqlite").toLowerCase();

        HikariConfig config = new HikariConfig();
        config.setPoolName("WildKits-Pool");
        config.setMaximumPoolSize(db.getInt("pool.maximum-pool-size", 10));
        config.setMinimumIdle(db.getInt("pool.minimum-idle", 2));
        config.setConnectionTimeout(db.getLong("pool.connection-timeout", 30000));
        config.setMaxLifetime(db.getLong("pool.max-lifetime", 1800000));

        if ("mysql".equals(type)) {
            String host = db.getString("mysql.host", "localhost");
            int port = db.getInt("mysql.port", 3306);
            String database = db.getString("mysql.database", "wildkits");
            String user = db.getString("mysql.username", "root");
            String password = db.getString("mysql.password", "");
            boolean useSSL = db.getBoolean("mysql.use-ssl", false);
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=" + useSSL + "&allowPublicKeyRetrieval=true&characterEncoding=utf8");
            config.setUsername(user);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            this.type = "sqlite";
            File file = new File(plugin.getDataFolder(), db.getString("sqlite.file", "wildkits.db"));
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
        }

        this.dataSource = new HikariDataSource(config);
        createTables();
        plugin.getLogger().info("Database connected (" + type + ").");
    }

    private void createTables() {
        String players = """
                CREATE TABLE IF NOT EXISTS wildkits_players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(16) NOT NULL,
                    coins BIGINT NOT NULL DEFAULT 0,
                    kills INT NOT NULL DEFAULT 0,
                    deaths INT NOT NULL DEFAULT 0,
                    killstreak INT NOT NULL DEFAULT 0,
                    best_killstreak INT NOT NULL DEFAULT 0,
                    level INT NOT NULL DEFAULT 1,
                    xp INT NOT NULL DEFAULT 0,
                    current_kit VARCHAR(64),
                    active_trail VARCHAR(64),
                    active_death_effect VARCHAR(64),
                    active_victory_effect VARCHAR(64),
                    active_tag VARCHAR(64),
                    active_title VARCHAR(64),
                    unlocked_kits TEXT,
                    unlocked_cosmetics TEXT,
                    favorites TEXT,
                    recent_kits TEXT,
                    last_daily BIGINT NOT NULL DEFAULT 0,
                    playtime_seconds BIGINT NOT NULL DEFAULT 0
                )
                """;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(players);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create tables", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public String getType() {
        return type;
    }

    public void executeAsync(SqlConsumer consumer) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                consumer.accept(connection);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Async SQL error", e);
            }
        });
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(Connection connection) throws SQLException;
    }
}
