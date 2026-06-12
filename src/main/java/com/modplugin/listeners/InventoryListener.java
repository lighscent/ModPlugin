package com.modplugin.listeners;

import com.modplugin.ModPlugin;
import com.modplugin.managers.InventoryViewer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class InventoryListener implements Listener {

    private final ModPlugin plugin;
    private final InventoryViewer inventoryViewer;

    public InventoryListener(ModPlugin plugin, InventoryViewer inventoryViewer) {
        this.plugin = plugin;
        this.inventoryViewer = inventoryViewer;
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
}
