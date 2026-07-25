package com.blaze.wildkits.database;

import com.blaze.wildkits.BlazesWildKits;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Production database layer for Blaze's WildKits.
 *
 * <p>SQLite uses the official {@code org.xerial:sqlite-jdbc} driver.
 * The {@code org.sqlite} package must NEVER be relocated by the shade plugin,
 * otherwise Linux native extraction fails with {@code UnsatisfiedLinkError}
 * in {@code NativeDB._open_utf8}.</p>
 */
public final class DatabaseManager {

    private static final int SCHEMA_VERSION = 3;

    private final BlazesWildKits plugin;
    private HikariDataSource dataSource;
    private String type = "sqlite";
    private String jdbcUrl = "";
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean memoryFallback = new AtomicBoolean(false);
    private String lastError = "";

    public DatabaseManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempts to connect. Never throws to the plugin enable path.
     *
     * @return true when a usable datasource is available
     */
    public boolean connect() {
        connected.set(false);
        memoryFallback.set(false);
        lastError = "";

        FileConfiguration db = plugin.getConfigManager().getDatabase();
        this.type = db.getString("type", "sqlite").toLowerCase();

        try {
            if ("mysql".equals(type)) {
                connectMySql(db);
            } else {
                this.type = "sqlite";
                connectSqlite(db);
            }
            validateConnection();
            migrateAndCreateSchema();
            connected.set(true);
            logSuccess();
            return true;
        } catch (Throwable primary) {
            lastError = rootMessage(primary);
            plugin.getLogger().severe("=================================================");
            plugin.getLogger().severe(" WildKits database failed to initialize!");
            plugin.getLogger().severe(" Type: " + type);
            plugin.getLogger().severe(" Reason: " + lastError);
            if (isNativeLinkError(primary)) {
                plugin.getLogger().severe(" Hint: SQLite native library failed to load.");
                plugin.getLogger().severe("       Ensure org.sqlite is NOT relocated in the plugin JAR.");
                plugin.getLogger().severe("       Official dependency: org.xerial:sqlite-jdbc");
            }
            plugin.getLogger().log(Level.SEVERE, "Database stacktrace", primary);
            plugin.getLogger().severe("=================================================");

            // Advanced recovery: try a clean local SQLite file once more, then memory mode
            if (!"mysql".equals(type)) {
                if (tryEmergencySqlite(db)) {
                    return true;
                }
            }
            return enableMemoryFallback(primary);
        }
    }

    private void connectSqlite(FileConfiguration db) throws Exception {
        ensureSqliteDriver();

        File file = resolveSqliteFile(db);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new SQLException("Unable to create database directory: " + parent.getAbsolutePath());
        }

        this.jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath();
        plugin.getLogger().info("Initializing SQLite at " + file.getAbsolutePath());

        // Probe native open BEFORE handing to Hikari so we surface clear errors
        probeSqlite(jdbcUrl);

        HikariConfig config = basePoolConfig(db);
        config.setPoolName("WildKits-SQLite");
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        // SQLite: single writer connection is safest and avoids lock storms
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(db.getLong("pool.connection-timeout", 15_000L));
        config.setIdleTimeout(0);
        config.setMaxLifetime(0);
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);

        // Advanced SQLite pragmas via connection init
        config.setConnectionInitSql(buildSqliteInitSql(db));

        // DataSource properties understood by sqlite-jdbc
        config.addDataSourceProperty("journal_mode", db.getString("sqlite.journal-mode", "WAL"));
        config.addDataSourceProperty("synchronous", db.getString("sqlite.synchronous", "NORMAL"));
        config.addDataSourceProperty("busy_timeout", String.valueOf(db.getInt("sqlite.busy-timeout-ms", 10_000)));
        config.addDataSourceProperty("foreign_keys", "true");

        closeQuietly();
        this.dataSource = new HikariDataSource(config);
        applyRuntimePragmas();
    }

    private void connectMySql(FileConfiguration db) throws Exception {
        ensureDriver("com.mysql.cj.jdbc.Driver");

        String host = db.getString("mysql.host", "localhost");
        int port = db.getInt("mysql.port", 3306);
        String database = db.getString("mysql.database", "wildkits");
        String user = db.getString("mysql.username", "root");
        String password = db.getString("mysql.password", "");
        boolean useSSL = db.getBoolean("mysql.use-ssl", false);

        this.jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSSL
                + "&allowPublicKeyRetrieval=true"
                + "&characterEncoding=utf8"
                + "&useUnicode=true"
                + "&serverTimezone=UTC";

        HikariConfig config = basePoolConfig(db);
        config.setPoolName("WildKits-MySQL");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(Math.max(2, db.getInt("pool.maximum-pool-size", 10)));
        config.setMinimumIdle(Math.max(1, db.getInt("pool.minimum-idle", 2)));
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");

        closeQuietly();
        this.dataSource = new HikariDataSource(config);
    }

    private HikariConfig basePoolConfig(FileConfiguration db) {
        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(db.getLong("pool.connection-timeout", 30_000L));
        config.setMaxLifetime(db.getLong("pool.max-lifetime", 1_800_000L));
        config.setInitializationFailTimeout(10_000L);
        config.setLeakDetectionThreshold(db.getLong("pool.leak-detection-ms", 0L));
        config.setPoolName("WildKits-Pool");
        return config;
    }

    private String buildSqliteInitSql(FileConfiguration db) {
        String journal = db.getString("sqlite.journal-mode", "WAL");
        String sync = db.getString("sqlite.synchronous", "NORMAL");
        int busy = db.getInt("sqlite.busy-timeout-ms", 10_000);
        // Single statement only for connectionInitSql; remaining applied in applyRuntimePragmas
        return "PRAGMA busy_timeout=" + Math.max(1000, busy) + ";";
    }

    private void applyRuntimePragmas() {
        if (dataSource == null || !"sqlite".equals(type)) return;
        FileConfiguration db = plugin.getConfigManager().getDatabase();
        String journal = db.getString("sqlite.journal-mode", "WAL");
        String sync = db.getString("sqlite.synchronous", "NORMAL");
        try (Connection connection = dataSource.getConnection(); Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("PRAGMA journal_mode=" + sanitizePragma(journal));
            st.execute("PRAGMA synchronous=" + sanitizePragma(sync));
            st.execute("PRAGMA temp_store=MEMORY");
            st.execute("PRAGMA mmap_size=268435456");
            st.execute("PRAGMA cache_size=-8000");
        } catch (SQLException e) {
            plugin.getLogger().warning("Could not apply all SQLite pragmas: " + e.getMessage());
        }
    }

    private static String sanitizePragma(String value) {
        if (value == null) return "NORMAL";
        return value.replaceAll("[^A-Za-z0-9_]", "");
    }

    private File resolveSqliteFile(FileConfiguration db) {
        String configured = db.getString("sqlite.file", "wildkits.db");
        File file = new File(configured);
        if (!file.isAbsolute()) {
            file = new File(plugin.getDataFolder(), configured);
        }
        return file;
    }

    private void ensureSqliteDriver() throws Exception {
        // Force class load from the shaded (but unrelocated) org.sqlite package
        Class.forName("org.sqlite.JDBC");
        Class.forName("org.sqlite.core.NativeDB");

        boolean registered = false;
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals("org.sqlite.JDBC")) {
                registered = true;
                break;
            }
        }
        if (!registered) {
            DriverManager.registerDriver((Driver) Class.forName("org.sqlite.JDBC").getDeclaredConstructor().newInstance());
        }

        plugin.getLogger().info("SQLite JDBC driver ready (" + org.sqlite.SQLiteJDBCLoader.getVersion() + ")");
        plugin.getLogger().info("SQLite native library loaded: " + org.sqlite.SQLiteJDBCLoader.isNativeMode());
    }

    private void ensureDriver(String className) throws Exception {
        Class.forName(className);
    }

    private void probeSqlite(String url) throws SQLException {
        Properties props = new Properties();
        props.setProperty("busy_timeout", "5000");
        try (Connection connection = DriverManager.getConnection(url, props);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT 1")) {
            if (!rs.next() || rs.getInt(1) != 1) {
                throw new SQLException("SQLite probe query returned unexpected result");
            }
        }
    }

    private void validateConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is null after connect");
        }
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            plugin.getLogger().info("JDBC: " + meta.getDriverName() + " " + meta.getDriverVersion());
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery("SELECT 1")) {
                rs.next();
            }
        }
    }

    private void migrateAndCreateSchema() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS wildkits_meta (
                        key VARCHAR(64) PRIMARY KEY,
                        value VARCHAR(255) NOT NULL
                    )
                    """);

            statement.execute("""
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
                        playtime_seconds BIGINT NOT NULL DEFAULT 0,
                        active_kill_effect VARCHAR(64),
                        active_prefix VARCHAR(64),
                        crate_keys TEXT,
                        quest_progress TEXT,
                        quest_completed TEXT,
                        last_daily_quest_reset BIGINT NOT NULL DEFAULT 0,
                        last_weekly_quest_reset BIGINT NOT NULL DEFAULT 0
                    )
                    """);

            // Advanced: combat history for analytics / anti-abuse
            if ("mysql".equals(type)) {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS wildkits_kill_log (
                            id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            killer_uuid VARCHAR(36) NOT NULL,
                            victim_uuid VARCHAR(36) NOT NULL,
                            kit_id VARCHAR(64),
                            coins_earned INT NOT NULL DEFAULT 0,
                            created_at BIGINT NOT NULL,
                            INDEX idx_killer_created (killer_uuid, created_at)
                        )
                        """);
            } else {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS wildkits_kill_log (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            killer_uuid VARCHAR(36) NOT NULL,
                            victim_uuid VARCHAR(36) NOT NULL,
                            kit_id VARCHAR(64),
                            coins_earned INT NOT NULL DEFAULT 0,
                            created_at BIGINT NOT NULL
                        )
                        """);
                statement.execute("CREATE INDEX IF NOT EXISTS idx_kill_log_killer ON wildkits_kill_log(killer_uuid, created_at)");
            }

            int current = readSchemaVersion(connection);
            if (current < SCHEMA_VERSION) {
                if (current < 2) {
                    ensureColumn(connection, "wildkits_players", "best_killstreak", "INT NOT NULL DEFAULT 0");
                    ensureColumn(connection, "wildkits_players", "playtime_seconds", "BIGINT NOT NULL DEFAULT 0");
                }
                if (current < 3) {
                    ensureColumn(connection, "wildkits_players", "active_kill_effect", "VARCHAR(64)");
                    ensureColumn(connection, "wildkits_players", "active_prefix", "VARCHAR(64)");
                    ensureColumn(connection, "wildkits_players", "crate_keys", "TEXT");
                    ensureColumn(connection, "wildkits_players", "quest_progress", "TEXT");
                    ensureColumn(connection, "wildkits_players", "quest_completed", "TEXT");
                    ensureColumn(connection, "wildkits_players", "last_daily_quest_reset", "BIGINT NOT NULL DEFAULT 0");
                    ensureColumn(connection, "wildkits_players", "last_weekly_quest_reset", "BIGINT NOT NULL DEFAULT 0");
                }
                writeSchemaVersion(connection, SCHEMA_VERSION);
                plugin.getLogger().info("Database schema migrated to v" + SCHEMA_VERSION + ".");
            }
        }
    }

    private int readSchemaVersion(Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT value FROM wildkits_meta WHERE key = 'schema_version'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.parseInt(rs.getString(1));
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private void writeSchemaVersion(Connection connection, int version) throws SQLException {
        if ("mysql".equals(type)) {
            try (PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO wildkits_meta(key, value) VALUES('schema_version', ?)
                    ON DUPLICATE KEY UPDATE value = VALUES(value)
                    """)) {
                ps.setString(1, String.valueOf(version));
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO wildkits_meta(key, value) VALUES('schema_version', ?)
                    ON CONFLICT(key) DO UPDATE SET value = excluded.value
                    """)) {
                ps.setString(1, String.valueOf(version));
                ps.executeUpdate();
            }
        }
    }

    private void ensureColumn(Connection connection, String table, String column, String definition) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, table, column)) {
                if (rs.next()) return;
            }
            try (Statement st = connection.createStatement()) {
                st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                plugin.getLogger().info("Added missing column " + table + "." + column);
            }
        } catch (SQLException e) {
            // SQLite may already have the column from CREATE TABLE IF NOT EXISTS
            plugin.getLogger().fine("ensureColumn " + column + ": " + e.getMessage());
        }
    }

    private boolean tryEmergencySqlite(FileConfiguration db) {
        plugin.getLogger().warning("Attempting emergency SQLite recovery...");
        try {
            closeQuietly();
            File emergency = new File(plugin.getDataFolder(), "wildkits-recovery.db");
            this.type = "sqlite";
            this.jdbcUrl = "jdbc:sqlite:" + emergency.getAbsolutePath();
            ensureSqliteDriver();
            probeSqlite(jdbcUrl);

            HikariConfig config = new HikariConfig();
            config.setPoolName("WildKits-SQLite-Recovery");
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.setMinimumIdle(1);
            config.setConnectionTestQuery("SELECT 1");
            config.setConnectionInitSql("PRAGMA busy_timeout=10000;");
            this.dataSource = new HikariDataSource(config);
            migrateAndCreateSchema();
            connected.set(true);
            plugin.getLogger().warning("Emergency SQLite recovery succeeded: " + emergency.getAbsolutePath());
            return true;
        } catch (Throwable recovery) {
            plugin.getLogger().log(Level.SEVERE, "Emergency SQLite recovery failed: " + rootMessage(recovery), recovery);
            closeQuietly();
            return false;
        }
    }

    private boolean enableMemoryFallback(Throwable cause) {
        plugin.getLogger().warning("Enabling in-memory fallback mode. Player data will NOT persist.");
        try {
            closeQuietly();
            this.type = "memory";
            this.jdbcUrl = "jdbc:sqlite:file:wildkits-mem?mode=memory&cache=shared";
            ensureSqliteDriver();
            probeSqlite(jdbcUrl);

            HikariConfig config = new HikariConfig();
            config.setPoolName("WildKits-Memory");
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.setMinimumIdle(1);
            config.setConnectionTestQuery("SELECT 1");
            this.dataSource = new HikariDataSource(config);
            migrateAndCreateSchema();
            connected.set(true);
            memoryFallback.set(true);
            plugin.getLogger().warning("Memory SQLite fallback is active. Fix disk SQLite and restart to persist data.");
            return true;
        } catch (Throwable memoryError) {
            closeQuietly();
            connected.set(false);
            memoryFallback.set(true);
            lastError = rootMessage(cause) + " | memory-fallback: " + rootMessage(memoryError);
            plugin.getLogger().severe("All database modes failed. Plugin continues in pure RAM cache mode.");
            plugin.getLogger().severe("Last error: " + lastError);
            return false;
        }
    }

    private void logSuccess() {
        String mode = memoryFallback.get() ? "memory-fallback" : type;
        plugin.getLogger().info("Database connected (" + mode + ") url=" + sanitizeUrl(jdbcUrl));
    }

    private static String sanitizeUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("password=[^&]*", "password=****");
    }

    private static boolean isNativeLinkError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof UnsatisfiedLinkError) return true;
            String message = String.valueOf(current.getMessage());
            if (message.contains("NativeDB") || message.contains("UnsatisfiedLinkError")
                    || message.contains("_open_utf8") || message.contains("native library")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        Throwable root = throwable;
        while (current != null) {
            root = current;
            current = current.getCause();
        }
        String msg = root.getMessage();
        return root.getClass().getSimpleName() + ": " + (msg == null ? "(no message)" : msg);
    }

    public boolean isConnected() {
        return connected.get() && dataSource != null && !dataSource.isClosed();
    }

    public boolean isMemoryFallback() {
        return memoryFallback.get();
    }

    public String getLastError() {
        return lastError;
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized" + (lastError.isEmpty() ? "" : " (" + lastError + ")"));
        }
        return dataSource.getConnection();
    }

    public void disconnect() {
        closeQuietly();
        connected.set(false);
    }

    private void closeQuietly() {
        if (dataSource != null) {
            try {
                if (!dataSource.isClosed()) {
                    dataSource.close();
                }
            } catch (Exception ignored) {
            }
            dataSource = null;
        }
    }

    public String getType() {
        return type;
    }

    public void executeAsync(SqlConsumer consumer) {
        if (!isConnected()) {
            plugin.getLogger().warning("Skipped SQL task: database unavailable (" + lastError + ")");
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection()) {
                consumer.accept(connection);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Async SQL error", e);
            }
        });
    }

    public void logKillAsync(String killerUuid, String victimUuid, String kitId, int coins) {
        if (!isConnected()) return;
        long now = System.currentTimeMillis();
        executeAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO wildkits_kill_log(killer_uuid, victim_uuid, kit_id, coins_earned, created_at) VALUES (?,?,?,?,?)")) {
                ps.setString(1, killerUuid);
                ps.setString(2, victimUuid);
                ps.setString(3, kitId);
                ps.setInt(4, coins);
                ps.setLong(5, now);
                ps.executeUpdate();
            }
        });
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(Connection connection) throws SQLException;
    }
}
