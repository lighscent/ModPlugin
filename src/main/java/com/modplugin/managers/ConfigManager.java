package com.modplugin.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ConfigManager {

    private FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public void reload(FileConfiguration config) {
        this.config = config;
    }

    public int slot(String key) {
        return config.getInt("slots." + key, 0);
    }

    public int freezeSlot() { return slot("freeze"); }
    public int inventorySlot() { return slot("inventory"); }
    public int enderchestSlot() { return slot("enderchest"); }
    public int teleportSlot() { return slot("teleport"); }
    public int vanishSlot() { return slot("vanish"); }
    public int quitSlot() { return slot("quit"); }
    public int pickupSlot() { return slot("pickup"); }

    public boolean isSilentJoinEnabled() {
        return config.getBoolean("silent-join.enabled", false);
    }

    public String getSilentJoinPermission() {
        return config.getString("silent-join.permission", "modplugin.silentjoin");
    }

    public boolean hasSilentJoin(Player player) {
        return isSilentJoinEnabled() && player.hasPermission(getSilentJoinPermission());
    }

    public boolean isAutoVanishEnabled() {
        return config.getBoolean("auto-vanish.enabled", false);
    }

    public String getAutoVanishPermission() {
        return config.getString("auto-vanish.permission", "modplugin.autovanish");
    }

    public boolean hasAutoVanish(Player player) {
        return isAutoVanishEnabled() && player.hasPermission(getAutoVanishPermission());
    }
}
