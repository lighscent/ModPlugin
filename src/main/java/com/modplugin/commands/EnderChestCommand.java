package com.modplugin.commands;

import com.modplugin.managers.InventoryViewer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnderChestCommand implements TabExecutor {

    private final InventoryViewer inventoryViewer;

    public EnderChestCommand(InventoryViewer inventoryViewer) {
        this.inventoryViewer = inventoryViewer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = CommandUtil.requirePlayer(sender);
        if (player == null || !CommandUtil.requirePermission(sender, "modplugin.enderchest.see")) return true;
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target != null) inventoryViewer.openPlayerEnderChest(player, target);
        else inventoryViewer.openOfflinePlayerEnderChest(player, args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Stream.concat(
                    Bukkit.getOnlinePlayers().stream().map(Player::getName),
                    Stream.of(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName)
            ).distinct().filter(name -> name.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
