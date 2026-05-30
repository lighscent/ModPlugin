package com.modplugin.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MuteManager {

    private final Set<UUID> mutedPlayers = new HashSet<>();

    public boolean isMuted(Player player) {
        return mutedPlayers.contains(player.getUniqueId());
    }

    public void toggleMute(Player target) {
        if (mutedPlayers.contains(target.getUniqueId())) {
            unmutePlayer(target);
        } else {
            mutePlayer(target);
        }
    }

    public void mutePlayer(Player target) {
        mutedPlayers.add(target.getUniqueId());
        target.sendMessage(ChatColor.RED + "You have been muted.");
    }

    public void unmutePlayer(Player target) {
        mutedPlayers.remove(target.getUniqueId());
        target.sendMessage(ChatColor.GREEN + "You have been unmuted.");
    }

    public void unmuteOnQuit(Player player) {
        mutedPlayers.remove(player.getUniqueId());
    }
}
