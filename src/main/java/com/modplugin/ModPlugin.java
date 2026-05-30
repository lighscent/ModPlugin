package com.modplugin;

import com.modplugin.commands.EnderChestCommand;
import com.modplugin.commands.FreezeCommand;
import com.modplugin.commands.GamemodeCommand;
import com.modplugin.commands.InventoryCommand;
import com.modplugin.commands.ModPluginCommand;
import com.modplugin.commands.MuteCommand;
import com.modplugin.commands.StaffCommand;
import com.modplugin.commands.VanishCommand;
import com.modplugin.database.DatabaseManager;
import com.modplugin.listeners.StaffModeListener;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();

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

        staffModeManager = new StaffModeManager(databaseManager, getLogger(), getConfig());
        staffModeManager.createTable();

        vanishManager = new VanishManager(getConfig());
        freezeManager = new FreezeManager();
        muteManager = new MuteManager();
        snapshotManager = new PlayerSnapshotManager(databaseManager, getLogger());
        snapshotManager.createTable();
        inventoryViewer = new InventoryViewer(databaseManager, getLogger());

        staffModeManager.setVanishManager(vanishManager);

        getCommand("staff").setExecutor(new StaffCommand(staffModeManager));
        getCommand("vanish").setExecutor(new VanishCommand(vanishManager));
        getCommand("freeze").setExecutor(new FreezeCommand(freezeManager));
        getCommand("mute").setExecutor(new MuteCommand(muteManager));
        getCommand("inventory").setExecutor(new InventoryCommand(inventoryViewer));
        getCommand("enderchest").setExecutor(new EnderChestCommand(inventoryViewer));
        getCommand("modplugin").setExecutor(new ModPluginCommand(this, staffModeManager));

        getServer().getPluginManager().registerEvents(
                new StaffModeListener(this, staffModeManager, vanishManager, freezeManager,
                        muteManager, snapshotManager, inventoryViewer), this);

        getLogger().info("ModPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (snapshotManager != null) {
            snapshotManager.saveAllOnlinePlayers();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("ModPlugin has been disabled!");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StaffModeManager getStaffModeManager() {
        return staffModeManager;
    }
}
