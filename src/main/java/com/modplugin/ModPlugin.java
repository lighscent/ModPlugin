package com.modplugin;

import com.modplugin.commands.*;
import com.modplugin.database.DatabaseManager;
import com.modplugin.listeners.InventoryListener;
import com.modplugin.listeners.PlayerStateListener;
import com.modplugin.listeners.StaffInteractionListener;
import com.modplugin.managers.*;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class ModPlugin extends JavaPlugin {

    private final Map<String, GameMode> commandModes = new HashMap<>();
    private DatabaseManager databaseManager;
    private StaffModeManager staffModeManager;
    private VanishManager vanishManager;
    private FreezeManager freezeManager;
    private MuteManager muteManager;
    private PlayerSnapshotManager snapshotManager;
    private InventoryViewer inventoryViewer;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(getConfig());

        commandModes.put("gmc", GameMode.CREATIVE);
        commandModes.put("gms", GameMode.SURVIVAL);
        commandModes.put("gmsp", GameMode.SPECTATOR);
        commandModes.put("gma", GameMode.ADVENTURE);

        GamemodeCommand gamemodeExecutor = new GamemodeCommand(commandModes);
        getCommand("gmc").setExecutor(gamemodeExecutor);
        getCommand("gms").setExecutor(gamemodeExecutor);
        getCommand("gmsp").setExecutor(gamemodeExecutor);
        getCommand("gma").setExecutor(gamemodeExecutor);

        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        freezeManager = new FreezeManager();
        muteManager = new MuteManager();
        vanishManager = new VanishManager(configManager);
        snapshotManager = new PlayerSnapshotManager(databaseManager, getLogger());
        snapshotManager.createTable();
        inventoryViewer = new InventoryViewer(databaseManager, getLogger());

        staffModeManager = new StaffModeManager(databaseManager, getLogger(), configManager);
        staffModeManager.setVanishManager(vanishManager);
        staffModeManager.createTable();

        registerCommands();
        registerListeners();

        getLogger().info("ModPlugin has been enabled!");
    }

    private void registerCommands() {
        getCommand("staff").setExecutor(new StaffCommand(staffModeManager));
        getCommand("vanish").setExecutor(new VanishCommand(vanishManager));
        getCommand("freeze").setExecutor(new FreezeCommand(freezeManager));
        getCommand("mute").setExecutor(new MuteCommand(muteManager));
        getCommand("inventory").setExecutor(new InventoryCommand(inventoryViewer));
        getCommand("enderchest").setExecutor(new EnderChestCommand(inventoryViewer));
        getCommand("modplugin").setExecutor(new ModPluginCommand(this, staffModeManager));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new StaffInteractionListener(staffModeManager, vanishManager, freezeManager, inventoryViewer), this);
        getServer().getPluginManager().registerEvents(
                new PlayerStateListener(staffModeManager, vanishManager, freezeManager, muteManager, snapshotManager), this);
        getServer().getPluginManager().registerEvents(
                new InventoryListener(this, inventoryViewer), this);
    }

    @Override
    public void onDisable() {
        if (snapshotManager != null) snapshotManager.saveAllOnlinePlayers();
        if (databaseManager != null) databaseManager.close();
        getLogger().info("ModPlugin has been disabled!");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StaffModeManager getStaffModeManager() {
        return staffModeManager;
    }
}
