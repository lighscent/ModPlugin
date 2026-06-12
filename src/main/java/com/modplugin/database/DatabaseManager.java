package com.modplugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            File dbFolder = new File(plugin.getDataFolder(), "db");
            dbFolder.mkdirs();
            File dbFile = new File(dbFolder, "database");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:file:" + dbFile.getAbsolutePath() + ";DB_CLOSE_ON_EXIT=FALSE");
            config.setDriverClassName("org.h2.Driver");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(5000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(600000);

            dataSource = new HikariDataSource(config);
            plugin.getLogger().info("H2 database connected via HikariCP.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize HikariCP connection pool", e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("HikariCP connection pool closed.");
        }
    }

    public void executeUpdate(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to execute update: " + sql, e);
        }
    }

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParameters(stmt, params);
        ResultSet rs = stmt.executeQuery();
        ClassLoader cl = getClass().getClassLoader();
        return (ResultSet) Proxy.newProxyInstance(cl, new Class[]{ResultSet.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) {
                        try {
                            return method.invoke(rs, args);
                        } finally {
                            stmt.close();
                            conn.close();
                        }
                    }
                    return method.invoke(rs, args);
                });
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}
