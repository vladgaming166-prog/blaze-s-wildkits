package com.blaze.wildkits.database;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Standalone Linux verification that the shaded production JAR can extract and
 * load the official Xerial SQLite native library (org.sqlite.core.NativeDB).
 *
 * <p>Run: {@code java -cp BlazesWildKits-*.jar com.blaze.wildkits.database.SqliteNativeSmokeTest}</p>
 */
public final class SqliteNativeSmokeTest {

    private SqliteNativeSmokeTest() {}

    public static void main(String[] args) throws Exception {
        System.out.println("== WildKits SQLite Native Smoke Test ==");
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("Java: " + System.getProperty("java.version"));

        Class<?> jdbc = Class.forName("org.sqlite.JDBC");
        Class<?> nativeDb = Class.forName("org.sqlite.core.NativeDB");
        System.out.println("Loaded driver class: " + jdbc.getName());
        System.out.println("Loaded native class: " + nativeDb.getName());

        // Relocated packages would look like com.blaze.wildkits.lib.sqlite...
        if (nativeDb.getName().startsWith("com.blaze.wildkits.lib.sqlite")) {
            throw new IllegalStateException("FAIL: org.sqlite was relocated — native loading will break on Linux");
        }

        boolean nativeMode = org.sqlite.SQLiteJDBCLoader.isNativeMode();
        System.out.println("SQLiteJDBCLoader version: " + org.sqlite.SQLiteJDBCLoader.getVersion());
        System.out.println("Native mode: " + nativeMode);
        if (!nativeMode) {
            // initialize explicitly
            org.sqlite.SQLiteJDBCLoader.initialize();
            nativeMode = org.sqlite.SQLiteJDBCLoader.isNativeMode();
            System.out.println("Native mode after initialize(): " + nativeMode);
        }
        if (!nativeMode) {
            throw new IllegalStateException("FAIL: SQLite native library did not load");
        }

        File temp = Files.createTempFile("wildkits-smoke-", ".db").toFile();
        temp.deleteOnExit();
        String url = "jdbc:sqlite:" + temp.getAbsolutePath();
        System.out.println("Opening " + url);

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS smoke(id INTEGER PRIMARY KEY, note TEXT)");
            statement.execute("INSERT INTO smoke(note) VALUES('wildkits-ok')");
            try (ResultSet rs = statement.executeQuery("SELECT note FROM smoke LIMIT 1")) {
                if (!rs.next() || !"wildkits-ok".equals(rs.getString(1))) {
                    throw new IllegalStateException("FAIL: unexpected query result");
                }
            }
            // Force native open path used in production errors
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA busy_timeout=5000");
        }

        // Also verify native resource path exists inside this JAR
        var resource = SqliteNativeSmokeTest.class.getClassLoader()
                .getResource("org/sqlite/native/Linux/x86_64/libsqlitejdbc.so");
        if (resource == null) {
            // fallback naming variations across sqlite-jdbc versions
            resource = SqliteNativeSmokeTest.class.getClassLoader()
                    .getResource("org/sqlite/native/Linux-x86_64/libsqlitejdbc.so");
        }
        System.out.println("Native resource present: " + (resource != null));
        if (resource != null) {
            System.out.println("Native resource URL: " + resource);
        }

        System.out.println("PASS: SQLite native extraction and NativeDB._open_utf8 work on Linux");
    }
}
