package com.modplugin.listeners;

import com.modplugin.ModPlugin;
import com.modplugin.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class StaffModeListener implements Listener {

    private final ModPlugin plugin;
    private final StaffModeManager staffModeManager;
    private final VanishManager vanishManager;
    private final FreezeManager freezeManager;
    private final MuteManager muteManager;
    private final PlayerSnapshotManager snapshotManager;
    private final InventoryViewer inventoryViewer;

    public StaffModeListener(ModPlugin plugin, StaffModeManager staffModeManager,
                             VanishManager vanishManager, FreezeManager freezeManager,
                             MuteManager muteManager, PlayerSnapshotManager snapshotManager,
                             InventoryViewer inventoryViewer) {
        this.plugin = plugin;
        this.staffModeManager = staffModeManager;
        this.vanishManager = vanishManager;
        this.freezeManager = freezeManager;
        this.muteManager = muteManager;
        this.snapshotManager = snapshotManager;
        this.inventoryViewer = inventoryViewer;
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
        if (staffModeManager.hasSilentJoin(player) || staffModeManager.isInStaffMode(player) || vanishManager.isVanished(player)) {
            event.setJoinMessage(null);
        }
        if (staffModeManager.hasAutoVanish(player) && !vanishManager.isVanished(player)) {
            vanishManager.vanishPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!staffModeManager.isInStaffMode(player)) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        if (item.getType() == Material.EYE_OF_ENDER) {
            vanishManager.toggleVanish(player);
        } else if (item.getType() == Material.ENDER_PEARL) {
            staffModeManager.teleportToRandomPlayer(player);
        } else if (item.getType() == Material.BARRIER) {
            if (staffModeManager.isQuitPending(player)) {
                staffModeManager.setQuitPending(player, false);
                player.chat("/staff");
            } else {
                staffModeManager.setQuitPending(player, true);
                player.sendMessage("§cClick the barrier again to quit staff mode.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!staffModeManager.isInStaffMode(player)) return;
        if (!(event.getRightClicked() instanceof Player)) return;

        ItemStack item = player.getItemInHand();
        if (item == null) return;

        Player target = (Player) event.getRightClicked();

        if (item.getType() == Material.BLAZE_ROD) {
            freezeManager.toggleFreeze(target);
            player.sendMessage(ChatColor.GRAY + (freezeManager.isFrozen(target) ? "Frozen " : "Unfrozen ") + target.getName() + ".");
        } else if (item.getType() == Material.CHEST) {
            inventoryViewer.openPlayerInventory(player, target);
        } else if (item.getType() == Material.ENDER_CHEST) {
            inventoryViewer.openPlayerEnderChest(player, target);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.startsWith("§8Inventory: ") || title.startsWith("§8Ender Chest: ")) {
            if (!clicker.hasPermission("modplugin.inventory.modify")) {
                event.setCancelled(true);
                return;
            }
            if (title.startsWith("§8Inventory: ") && inventoryViewer.isViewingOnlineInventory(clicker)) {
                plugin.getServer().getScheduler().runTask(plugin, () -> inventoryViewer.applyOnlineInventory(clicker));
            } else if (title.startsWith("§8Ender Chest: ") && inventoryViewer.isViewingOnlineEnderChest(clicker)) {
                plugin.getServer().getScheduler().runTask(plugin, () -> inventoryViewer.applyOnlineEnderChest(clicker));
            }
        } else if (inventoryViewer.isWatched(clicker)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> inventoryViewer.refreshViewersOf(clicker));
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (inventoryViewer.isWatched(event.getPlayer())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> inventoryViewer.refreshViewersOf(event.getPlayer()));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (inventoryViewer.isWatched(event.getPlayer())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> inventoryViewer.refreshViewersOf(event.getPlayer()));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player viewer = (Player) event.getPlayer();
        if (inventoryViewer.isViewingOfflineInventory(viewer)) {
            inventoryViewer.saveOfflineInventoryOnClose(viewer);
        } else if (inventoryViewer.isViewingOfflineEnderChest(viewer)) {
            inventoryViewer.saveOfflineEnderOnClose(viewer);
        }
        inventoryViewer.removeOnlineInventoryViewer(viewer);
        inventoryViewer.removeOnlineEnderViewer(viewer);
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
        if (staffModeManager.hasSilentJoin(p) || staffModeManager.isInStaffMode(p) || vanishManager.isVanished(p)) {
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
