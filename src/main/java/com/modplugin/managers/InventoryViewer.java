package com.modplugin.managers;

import com.modplugin.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.modplugin.managers.SerializationUtil.*;

public class InventoryViewer {

    private final DatabaseManager db;
    private final Logger logger;
    private final Map<UUID, UUID> offlineInventoryViewers = new HashMap<>();
    private final Map<UUID, UUID> offlineEnderViewers = new HashMap<>();
    private final Map<UUID, UUID> onlineInventoryViewers = new HashMap<>();
    private final Map<UUID, UUID> onlineEnderViewers = new HashMap<>();
    private final Map<UUID, Set<UUID>> watchedPlayers = new HashMap<>();

    public InventoryViewer(DatabaseManager db, Logger logger) {
        this.db = db;
        this.logger = logger;
    }

    public void openPlayerInventory(Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Inventory: " + target.getName());
        copyInventory(target, inv);
        viewer.openInventory(inv);
        UUID viewerId = viewer.getUniqueId();
        UUID targetId = target.getUniqueId();
        onlineInventoryViewers.put(viewerId, targetId);
        watchedPlayers.computeIfAbsent(targetId, k -> new HashSet<>()).add(viewerId);
    }

    public void openPlayerEnderChest(Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Ender Chest: " + target.getName());
        inv.setContents(target.getEnderChest().getContents());
        viewer.openInventory(inv);
        UUID viewerId = viewer.getUniqueId();
        UUID targetId = target.getUniqueId();
        onlineEnderViewers.put(viewerId, targetId);
        watchedPlayers.computeIfAbsent(targetId, k -> new HashSet<>()).add(viewerId);
    }

    public void openOfflinePlayerInventory(Player viewer, String playerName) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
        if (!offline.hasPlayedBefore()) {
            viewer.sendMessage("§cPlayer has never played before.");
            return;
        }
        UUID uuid = offline.getUniqueId();
        try {
            ResultSet rs = db.executeQuery("SELECT * FROM player_snapshots WHERE uuid = ?", uuid.toString());
            if (!rs.next()) { rs.close(); viewer.sendMessage("§cNo saved data for this player."); return; }

            ItemStack[] inventory = deserializeItemStacks(rs.getString("inventory"));
            ItemStack[] armor = deserializeItemStacks(rs.getString("armor"));
            int level = rs.getInt("level");
            rs.close();

            Inventory inv = Bukkit.createInventory(null, 54, "§8Inventory: " + offline.getName());
            for (int i = 0; i < Math.min(inventory.length, 36); i++) inv.setItem(i, inventory[i]);
            for (int i = 0; i < Math.min(armor.length, 4); i++) inv.setItem(36 + i, armor[i]);

            ItemStack xpItem = new ItemStack(Material.EXP_BOTTLE);
            ItemMeta xpMeta = xpItem.getItemMeta();
            xpMeta.setDisplayName("§bXP: " + level);
            xpMeta.setLore(Arrays.asList("§7Offline player"));
            xpItem.setItemMeta(xpMeta);
            inv.setItem(41, xpItem);

            fillBorder(inv, 42);
            viewer.openInventory(inv);
            offlineInventoryViewers.put(viewer.getUniqueId(), uuid);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load offline inventory for " + playerName, e);
            viewer.sendMessage("§cFailed to load player data.");
        }
    }

    public void openOfflinePlayerEnderChest(Player viewer, String playerName) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
        if (!offline.hasPlayedBefore()) {
            viewer.sendMessage("§cPlayer has never played before.");
            return;
        }
        UUID uuid = offline.getUniqueId();
        try {
            ResultSet rs = db.executeQuery("SELECT enderchest FROM player_snapshots WHERE uuid = ?", uuid.toString());
            if (!rs.next()) { rs.close(); viewer.sendMessage("§cNo saved data for this player."); return; }

            ItemStack[] enderChest = deserializeItemStacks(rs.getString("enderchest"));
            rs.close();

            Inventory inv = Bukkit.createInventory(null, 27, "§8Ender Chest: " + offline.getName());
            for (int i = 0; i < Math.min(enderChest.length, 27); i++) inv.setItem(i, enderChest[i]);
            viewer.openInventory(inv);
            offlineEnderViewers.put(viewer.getUniqueId(), uuid);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load offline ender chest for " + playerName, e);
            viewer.sendMessage("§cFailed to load player data.");
        }
    }

    public boolean isViewingOfflineInventory(Player viewer) {
        return offlineInventoryViewers.containsKey(viewer.getUniqueId());
    }

    public boolean isViewingOfflineEnderChest(Player viewer) {
        return offlineEnderViewers.containsKey(viewer.getUniqueId());
    }

    public boolean isViewingOnlineInventory(Player viewer) {
        return onlineInventoryViewers.containsKey(viewer.getUniqueId());
    }

    public boolean isViewingOnlineEnderChest(Player viewer) {
        return onlineEnderViewers.containsKey(viewer.getUniqueId());
    }

    public boolean isWatched(Player target) {
        Set<UUID> viewers = watchedPlayers.get(target.getUniqueId());
        return viewers != null && !viewers.isEmpty();
    }

    public void refreshViewersOf(Player target) {
        Set<UUID> viewerIds = watchedPlayers.get(target.getUniqueId());
        if (viewerIds == null || viewerIds.isEmpty()) return;

        for (UUID viewerId : viewerIds) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer == null) continue;
            Inventory top = viewer.getOpenInventory().getTopInventory();
            if (top == null) continue;
            if (onlineInventoryViewers.containsKey(viewerId)) {
                copyInventory(target, top);
            } else if (onlineEnderViewers.containsKey(viewerId)) {
                top.setContents(target.getEnderChest().getContents());
            }
        }
    }

    private void copyInventory(Player source, Inventory target) {
        for (int i = 0; i < 36; i++) target.setItem(i, source.getInventory().getItem(i));
        target.setItem(36, source.getInventory().getHelmet());
        target.setItem(37, source.getInventory().getChestplate());
        target.setItem(38, source.getInventory().getLeggings());
        target.setItem(39, source.getInventory().getBoots());
        try {
            Method m = source.getInventory().getClass().getMethod("getItemInOffHand");
            target.setItem(40, (ItemStack) m.invoke(source.getInventory()));
        } catch (Exception e) {}
        ItemStack xpItem = target.getItem(41);
        if (xpItem != null && xpItem.getType() == Material.EXP_BOTTLE) {
            ItemMeta meta = xpItem.getItemMeta();
            meta.setDisplayName("§bXP: " + source.getLevel());
            meta.setLore(Arrays.asList("§7" + Math.round(source.getExp() * 100) + "% to next level"));
            xpItem.setItemMeta(meta);
            target.setItem(41, xpItem);
        }
    }

    public void saveOfflineInventoryOnClose(Player viewer) {
        UUID targetUUID = offlineInventoryViewers.remove(viewer.getUniqueId());
        if (targetUUID == null) return;
        try {
            Inventory top = viewer.getOpenInventory().getTopInventory();
            ItemStack[] inventory = new ItemStack[36];
            for (int i = 0; i < 36; i++) inventory[i] = top.getItem(i);
            ItemStack[] armor = {top.getItem(36), top.getItem(37), top.getItem(38), top.getItem(39)};
            db.executeUpdate(
                    "MERGE INTO player_snapshots (uuid, inventory, armor, enderchest, level, exp, total_exp) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    targetUUID.toString(), serializeItemStacks(inventory), serializeItemStacks(armor),
                    serializeItemStacks(new ItemStack[0]), 0, 0f, 0);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to save modified offline inventory", e);
        }
    }

    public void saveOfflineEnderOnClose(Player viewer) {
        UUID targetUUID = offlineEnderViewers.remove(viewer.getUniqueId());
        if (targetUUID == null) return;
        try {
            Inventory top = viewer.getOpenInventory().getTopInventory();
            ItemStack[] ender = new ItemStack[27];
            for (int i = 0; i < 27; i++) ender[i] = top.getItem(i);
            db.executeUpdate("UPDATE player_snapshots SET enderchest = ? WHERE uuid = ?",
                    serializeItemStacks(ender), targetUUID.toString());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to save modified offline ender chest", e);
        }
    }

    public void applyOnlineInventory(Player viewer) {
        UUID targetUUID = onlineInventoryViewers.get(viewer.getUniqueId());
        if (targetUUID == null) return;
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) return;
        try {
            Inventory top = viewer.getOpenInventory().getTopInventory();
            for (int i = 0; i < 36; i++) target.getInventory().setItem(i, top.getItem(i));
            target.getInventory().setHelmet(top.getItem(36));
            target.getInventory().setChestplate(top.getItem(37));
            target.getInventory().setLeggings(top.getItem(38));
            target.getInventory().setBoots(top.getItem(39));
            target.updateInventory();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to apply online inventory changes", e);
        }
    }

    public void applyOnlineEnderChest(Player viewer) {
        UUID targetUUID = onlineEnderViewers.get(viewer.getUniqueId());
        if (targetUUID == null) return;
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) return;
        try {
            Inventory top = viewer.getOpenInventory().getTopInventory();
            for (int i = 0; i < 27; i++) target.getEnderChest().setItem(i, top.getItem(i));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to apply online ender chest changes", e);
        }
    }

    public void removeOnlineInventoryViewer(Player viewer) {
        UUID targetId = onlineInventoryViewers.remove(viewer.getUniqueId());
        if (targetId != null) unwatch(targetId, viewer.getUniqueId());
    }

    public void removeOnlineEnderViewer(Player viewer) {
        UUID targetId = onlineEnderViewers.remove(viewer.getUniqueId());
        if (targetId != null) unwatch(targetId, viewer.getUniqueId());
    }

    private void unwatch(UUID targetId, UUID viewerId) {
        Set<UUID> viewers = watchedPlayers.get(targetId);
        if (viewers != null) {
            viewers.remove(viewerId);
            if (viewers.isEmpty()) watchedPlayers.remove(targetId);
        }
    }

    private void fillBorder(Inventory inv, int fromSlot) {
        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int i = fromSlot; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, filler);
            }
        }
    }
}
