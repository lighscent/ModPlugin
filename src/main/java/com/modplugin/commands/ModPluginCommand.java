package com.modplugin.commands;

import com.modplugin.ModPlugin;
import com.modplugin.managers.StaffModeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ModPluginCommand implements CommandExecutor {

    private final ModPlugin plugin;
    private final StaffModeManager staffModeManager;

    public ModPluginCommand(ModPlugin plugin, StaffModeManager staffModeManager) {
        this.plugin = plugin;
        this.staffModeManager = staffModeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("modplugin.reload")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "Usage: /modplugin reload");
            return true;
        }

        plugin.reloadConfig();
        staffModeManager.reloadConfig(plugin.getConfig());
        sender.sendMessage(ChatColor.GREEN + "ModPlugin config reloaded.");
        return true;
    }
}
