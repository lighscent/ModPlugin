package com.modplugin.listeners;

import com.modplugin.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class StaffInteractionListener implements Listener {

    private final StaffModeManager staffModeManager;
    private final VanishManager vanishManager;
    private final FreezeManager freezeManager;
    private final InventoryViewer inventoryViewer;

    public StaffInteractionListener(StaffModeManager staffModeManager, VanishManager vanishManager,
                                    FreezeManager freezeManager, InventoryViewer inventoryViewer) {
        this.staffModeManager = staffModeManager;
        this.vanishManager = vanishManager;
        this.freezeManager = freezeManager;
        this.inventoryViewer = inventoryViewer;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!staffModeManager.isInStaffMode(player)) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (item.getType() == Material.BLAZE_ROD || item.getType() == Material.CHEST || item.getType() == Material.ENDER_CHEST) {
            return; // handled by onPlayerInteractEntity
        }

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
        } else if (item.getType() == Material.HOPPER) {
            staffModeManager.togglePickup(player);
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
            if (freezeManager.toggleFreeze(target)) {
                player.sendMessage(ChatColor.GRAY + (freezeManager.isFrozen(target) ? "Frozen " : "Unfrozen ") + target.getName() + ".");
            }
        } else if (item.getType() == Material.CHEST) {
            inventoryViewer.openPlayerInventory(player, target);
        } else if (item.getType() == Material.ENDER_CHEST) {
            inventoryViewer.openPlayerEnderChest(player, target);
        }
    }
}
