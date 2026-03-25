package com.microfinance.Util;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            try (InputStream input = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("database.properties")) {
                if (input == null)
                    throw new RuntimeException(
                            "database.properties introuvable dans resources/"
                    );
                props.load(input);
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(
                    Integer.parseInt(props.getProperty("db.pool.size"))
            );
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            dataSource = new HikariDataSource(config);
            System.out.println("✅ HikariCP initialisé avec succès !");
        } catch (Exception e) {
            throw new RuntimeException(
                    "❌ Erreur initialisation HikariCP", e
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Pool HikariCP fermé.");
        }
    }
}
