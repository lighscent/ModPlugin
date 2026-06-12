package com.modplugin.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Map<UUID, Long> lastToggle = new HashMap<>();

    public boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public void toggleFreeze(Player target) {
        long now = System.currentTimeMillis();
        if (now - lastToggle.getOrDefault(target.getUniqueId(), 0L) < 300) return;
        lastToggle.put(target.getUniqueId(), now);
        if (frozenPlayers.contains(target.getUniqueId())) {
            unfreezePlayer(target);
        } else {
            freezePlayer(target);
        }
    }

    public void freezePlayer(Player target) {
        frozenPlayers.add(target.getUniqueId());
        target.setWalkSpeed(0);
        target.sendMessage(ChatColor.RED + "You have been frozen by a staff member.");
    }

    public void unfreezePlayer(Player target) {
        frozenPlayers.remove(target.getUniqueId());
        target.setWalkSpeed(0.2f);
        target.sendMessage(ChatColor.GREEN + "You have been unfrozen.");
    }

    public void unfreezeOnQuit(Player player) {
        if (frozenPlayers.remove(player.getUniqueId())) {
            player.setWalkSpeed(0.2f);
        }
    }
}
