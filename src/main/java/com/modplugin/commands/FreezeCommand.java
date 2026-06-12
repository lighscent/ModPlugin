package com.modplugin.commands;

import com.modplugin.managers.FreezeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {

    private final FreezeManager freezeManager;

    public FreezeCommand(FreezeManager freezeManager) {
        this.freezeManager = freezeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!CommandUtil.requirePermission(sender, "modplugin.freeze.use")) return true;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }
        Player target = CommandUtil.findPlayer(sender, args[0]);
        if (target == null) return true;
        freezeManager.toggleFreeze(target);
        sender.sendMessage(ChatColor.GRAY + (freezeManager.isFrozen(target) ? "Frozen " : "Unfrozen ") + target.getName() + ".");
        return true;
    }
}
