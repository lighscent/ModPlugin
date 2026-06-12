package com.modplugin.commands;

import com.modplugin.managers.StaffModeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffCommand implements CommandExecutor {

    private final StaffModeManager staffModeManager;

    public StaffCommand(StaffModeManager staffModeManager) {
        this.staffModeManager = staffModeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = CommandUtil.requirePlayer(sender);
        if (player == null || !CommandUtil.requirePermission(sender, "modplugin.staff")) return true;
        staffModeManager.toggleStaffMode(player);
        return true;
    }
}
