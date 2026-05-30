package com.modplugin.managers;

import com.modplugin.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.modplugin.managers.SerializationUtil.*;

public class PlayerSnapshotManager {

    private final DatabaseManager db;
    private final Logger logger;

    public PlayerSnapshotManager(DatabaseManager db, Logger logger) {
        this.db = db;
        this.logger = logger;
    }

    public void createTable() {
        db.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_snapshots (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "inventory CLOB, " +
                        "armor CLOB, " +
                        "enderchest CLOB, " +
                        "level INT, " +
                        "exp DOUBLE, " +
                        "total_exp INT)");
    }

    public void savePlayerSnapshot(Player player) {
        try {
            db.executeUpdate(
                    "MERGE INTO player_snapshots (uuid, inventory, armor, enderchest, level, exp, total_exp) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    player.getUniqueId().toString(),
                    serializeItemStacks(player.getInventory().getContents()),
                    serializeItemStacks(player.getInventory().getArmorContents()),
                    serializeItemStacks(player.getEnderChest().getContents()),
                    player.getLevel(), player.getExp(), player.getTotalExperience());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to save snapshot for " + player.getName(), e);
        }
    }

    public void saveAllOnlinePlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            savePlayerSnapshot(p);
        }
    }

    public boolean hasPlayerSnapshot(UUID uuid) {
        try {
            ResultSet rs = db.executeQuery("SELECT COUNT(*) FROM player_snapshots WHERE uuid = ?", uuid.toString());
            boolean exists = rs.next() && rs.getInt(1) > 0;
            rs.close();
            return exists;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to check snapshot for " + uuid, e);
            return false;
        }
    }

    public void applyPlayerSnapshot(Player player) {
        UUID uuid = player.getUniqueId();
        if (!hasPlayerSnapshot(uuid)) return;
        try {
            ResultSet rs = db.executeQuery("SELECT * FROM player_snapshots WHERE uuid = ?", uuid.toString());
            if (!rs.next()) { rs.close(); return; }

            ItemStack[] inventory = deserializeItemStacks(rs.getString("inventory"));
            ItemStack[] armor = deserializeItemStacks(rs.getString("armor"));
            ItemStack[] enderChest = deserializeItemStacks(rs.getString("enderchest"));
            int level = rs.getInt("level");
            float exp = rs.getFloat("exp");
            int totalExp = rs.getInt("total_exp");
            rs.close();

            player.getInventory().setContents(inventory);
            player.getInventory().setArmorContents(armor);
            player.getEnderChest().setContents(enderChest);
            player.setLevel(level);
            player.setExp(exp);
            player.setTotalExperience(totalExp);

            db.executeUpdate("DELETE FROM player_snapshots WHERE uuid = ?", uuid.toString());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to apply snapshot for " + player.getName(), e);
        }
    }
}
