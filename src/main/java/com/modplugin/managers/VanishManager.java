package com.modplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final ConfigManager config;

    public VanishManager(ConfigManager config) {
        this.config = config;
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public void toggleVanish(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) unvanishPlayer(player);
        else vanishPlayer(player);
    }

    public void vanishPlayer(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        setVanished(player, true);
        player.setPlayerListName("");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) p.hidePlayer(player);
        }
        updateVanishItem(player, true);
        player.sendMessage(ChatColor.GRAY + "You are now vanished.");
    }

    public void unvanishPlayer(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
        setVanished(player, false);
        player.setPlayerListName(player.getName());
        revealPlayer(player);
        updateVanishItem(player, false);
        player.sendMessage(ChatColor.GRAY + "You are no longer vanished.");
    }

    private void setVanished(Player player, boolean vanish) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        try {
            player.getClass().getMethod("setInvisible", boolean.class).invoke(player, vanish);
        } catch (Exception e) {
            if (vanish) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false));
            }
        }
    }

    private void updateVanishItem(Player player, boolean vanished) {
        int s = config.vanishSlot();
        ItemStack eye = player.getInventory().getItem(s);
        if (eye == null || eye.getType() != Material.EYE_OF_ENDER) return;
        ItemMeta meta = eye.getItemMeta();
        meta.setDisplayName(vanished ? "§aVanish ON" : "§cVanish OFF");
        eye.setItemMeta(meta);
        player.getInventory().setItem(s, eye);
    }

    public void hideFromVanished(Player joining) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (vanishedPlayers.contains(p.getUniqueId())) {
                joining.hidePlayer(p);
            }
        }
    }

    private void revealPlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) p.showPlayer(player);
        }
    }
}
