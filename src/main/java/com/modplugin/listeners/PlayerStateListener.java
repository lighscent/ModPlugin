package com.modplugin.listeners;

import com.modplugin.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerStateListener implements Listener {

    private final StaffModeManager staffModeManager;
    private final VanishManager vanishManager;
    private final FreezeManager freezeManager;
    private final MuteManager muteManager;
    private final PlayerSnapshotManager snapshotManager;

    public PlayerStateListener(StaffModeManager staffModeManager, VanishManager vanishManager,
                               FreezeManager freezeManager, MuteManager muteManager,
                               PlayerSnapshotManager snapshotManager) {
        this.staffModeManager = staffModeManager;
        this.vanishManager = vanishManager;
        this.freezeManager = freezeManager;
        this.muteManager = muteManager;
        this.snapshotManager = snapshotManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (staffModeManager.isInStaffMode(player) || freezeManager.isFrozen(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        snapshotManager.applyPlayerSnapshot(player);
        staffModeManager.restoreStaffMode(player);
        vanishManager.hideFromVanished(player);

        ConfigManager config = staffModeManager.getConfigManager();
        if (config.hasSilentJoin(player) || staffModeManager.isInStaffMode(player) || vanishManager.isVanished(player)) {
            event.setJoinMessage(null);
        }
        if (config.hasAutoVanish(player) && !vanishManager.isVanished(player)) {
            vanishManager.vanishPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!freezeManager.isFrozen(player)) return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            player.teleport(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        snapshotManager.savePlayerSnapshot(p);
        freezeManager.unfreezeOnQuit(p);
        muteManager.unmuteOnQuit(p);
        ConfigManager config = staffModeManager.getConfigManager();
        if (config.hasSilentJoin(p) || staffModeManager.isInStaffMode(p) || vanishManager.isVanished(p)) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (muteManager.isMuted(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You are muted.");
        }
    }
}
