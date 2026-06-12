package com.modplugin.commands;

import com.modplugin.managers.VanishManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final VanishManager vanishManager;

    public VanishCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = CommandUtil.requirePlayer(sender);
        if (player == null || !CommandUtil.requirePermission(sender, "modplugin.vanish.use")) return true;
        vanishManager.toggleVanish(player);
        return true;
    }
}
