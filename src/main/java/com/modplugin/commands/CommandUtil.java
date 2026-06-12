package com.modplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandUtil {

    private CommandUtil() {}

    public static Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player) return (Player) sender;
        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
        return null;
    }

    public static boolean requirePermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        sender.sendMessage(ChatColor.RED + "No permission.");
        return false;
    }

    public static Player findPlayer(CommandSender sender, String name) {
        if (name == null || name.isEmpty()) return null;
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) sender.sendMessage(ChatColor.RED + "Player not found.");
        return target;
    }
}
