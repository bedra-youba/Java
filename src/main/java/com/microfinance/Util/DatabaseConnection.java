package com.microfinance.Util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseConnection {
    private static HikariDataSource dataSource;

    static {
        try {
            // Charger le fichier properties (conforme page 114)
            Properties props = new Properties();
            try (InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("database.properties")) {
                props.load(input);
            }

            // Configurer HikariCP (conforme page 115-116)
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size")));

            // Optimisations PostgreSQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Erreur d'initialisation du pool", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // Prend une connexion du pool
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}