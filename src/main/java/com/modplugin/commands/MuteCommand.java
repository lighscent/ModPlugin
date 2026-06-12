package com.modplugin.commands;

import com.modplugin.managers.MuteManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteCommand implements CommandExecutor {

    private final MuteManager muteManager;

    public MuteCommand(MuteManager muteManager) {
        this.muteManager = muteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!CommandUtil.requirePermission(sender, "modplugin.mute.use")) return true;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <player>");
            return true;
        }
        Player target = CommandUtil.findPlayer(sender, args[0]);
        if (target == null) return true;
        muteManager.toggleMute(target);
        sender.sendMessage(ChatColor.GRAY + (muteManager.isMuted(target) ? "Muted " : "Unmuted ") + target.getName() + ".");
        return true;
    }
}
