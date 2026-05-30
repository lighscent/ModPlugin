package com.modplugin.commands;

import com.modplugin.managers.StaffModeManager;
import org.bukkit.ChatColor;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("modplugin.staff")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        Player player = (Player) sender;
        staffModeManager.toggleStaffMode(player);
        return true;
    }
}
