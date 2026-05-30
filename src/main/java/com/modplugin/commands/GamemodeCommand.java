package com.modplugin.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class GamemodeCommand implements CommandExecutor {

    private final Map<String, GameMode> commandModes;

    public GamemodeCommand(Map<String, GameMode> commandModes) {
        this.commandModes = commandModes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        GameMode mode = commandModes.get(cmd.getName());
        if (mode == null)
            return false;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (!sender.hasPermission("modplugin." + cmd.getName() + ".self")) {
                sender.sendMessage("No permission.");
                return true;
            }
            Player p = (Player) sender;
            p.setGameMode(mode);
            p.sendMessage("Gamemode updated to " + mode.name().toLowerCase() + ".");
            return true;
        }

        if (!sender.hasPermission("modplugin." + cmd.getName() + ".other")) {
            sender.sendMessage("No permission.");
            return true;
        }

        Player target = sender.getServer().getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        target.setGameMode(mode);
        target.sendMessage("Your gamemode has been updated to " + mode.name().toLowerCase() + ".");
        sender.sendMessage("Updated " + target.getName() + "'s gamemode.");
        return true;
    }
}
