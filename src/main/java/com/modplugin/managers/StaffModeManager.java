package com.modplugin.managers;

import com.modplugin.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.modplugin.managers.SerializationUtil.*;

public class StaffModeManager {

    private final DatabaseManager db;
    private final Logger logger;
    private final ConfigManager config;
    private final Set<UUID> staffModePlayers = new HashSet<>();
    private final Set<UUID> staffQuitPending = new HashSet<>();
    private VanishManager vanishManager;

    public StaffModeManager(DatabaseManager db, Logger logger, ConfigManager config) {
        this.db = db;
        this.logger = logger;
        this.config = config;
    }

    public void setVanishManager(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public void createTable() {
        db.executeUpdate("CREATE TABLE IF NOT EXISTS staff_data (" +
                "uuid VARCHAR(36) PRIMARY KEY, inventory CLOB, armor CLOB, level INT, " +
                "exp DOUBLE, total_exp INT, effects CLOB)");
        db.executeUpdate("ALTER TABLE staff_data ADD COLUMN IF NOT EXISTS world VARCHAR(64)");
        db.executeUpdate("ALTER TABLE staff_data ADD COLUMN IF NOT EXISTS x DOUBLE");
        db.executeUpdate("ALTER TABLE staff_data ADD COLUMN IF NOT EXISTS y DOUBLE");
        db.executeUpdate("ALTER TABLE staff_data ADD COLUMN IF NOT EXISTS z DOUBLE");
        db.executeUpdate("ALTER TABLE staff_data ADD COLUMN IF NOT EXISTS yaw DOUBLE");
        db.executeUpdate("ALTER TABLE staff_data ADD COLUMN IF NOT EXISTS pitch DOUBLE");
    }

    public boolean isInStaffMode(Player player) {
        return staffModePlayers.contains(player.getUniqueId());
    }

    public boolean hasSavedData(UUID uuid) {
        try {
            ResultSet rs = db.executeQuery("SELECT COUNT(*) FROM staff_data WHERE uuid = ?", uuid.toString());
            boolean exists = rs.next() && rs.getInt(1) > 0;
            rs.close();
            return exists;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to check staff data for " + uuid, e);
            return false;
        }
    }

    public boolean isQuitPending(Player player) {
        return staffQuitPending.contains(player.getUniqueId());
    }

    public void setQuitPending(Player player, boolean pending) {
        if (pending) {
            staffQuitPending.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("ModPlugin"), () ->
                    staffQuitPending.remove(player.getUniqueId()), 100L);
        } else {
            staffQuitPending.remove(player.getUniqueId());
        }
    }

    public void toggleStaffMode(Player player) {
        if (isInStaffMode(player)) disableStaffMode(player);
        else enableStaffMode(player);
    }

    public void enableStaffMode(Player player) {
        savePlayerData(player);
        staffModePlayers.add(player.getUniqueId());
        applyStaffItems(player);
        if (vanishManager != null) vanishManager.vanishPlayer(player);
        applyNightVision(player);
        player.sendMessage("§7[§aStaff§7] §fStaff mode enabled.");
    }

    public void disableStaffMode(Player player) {
        staffModePlayers.remove(player.getUniqueId());
        if (vanishManager != null && vanishManager.isVanished(player)) {
            vanishManager.unvanishPlayer(player);
        }
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        restorePlayerData(player);
        player.sendMessage("§7[§aStaff§7] §fStaff mode disabled.");
    }

    public void restoreStaffMode(Player player) {
        if (!hasSavedData(player.getUniqueId())) return;
        staffModePlayers.add(player.getUniqueId());
        applyStaffItems(player);
        if (vanishManager != null) vanishManager.vanishPlayer(player);
        applyNightVision(player);
        player.sendMessage("§7[§aStaff§7] §fStaff mode restored on reconnect.");
    }

    public void applyStaffItems(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.setLevel(0);
        player.setExp(0);
        player.setGameMode(GameMode.CREATIVE);

        inv.setItem(config.freezeSlot(), makeItem(Material.BLAZE_ROD, "§bFreeze"));
        inv.setItem(config.inventorySlot(), makeItem(Material.CHEST, "§bInventory"));
        inv.setItem(config.enderchestSlot(), makeItem(Material.ENDER_CHEST, "§bEnder Chest"));
        inv.setItem(config.teleportSlot(), makeItem(Material.ENDER_PEARL, "§bTeleport"));
        inv.setItem(config.vanishSlot(), makeItem(Material.EYE_OF_ENDER, "§bVanish"));
        inv.setItem(config.quitSlot(), makeItem(Material.BARRIER, "§cQuit Staff Mode"));
    }

    private ItemStack makeItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void applyNightVision(Player player) {
        try {
            Constructor<?> ctor = PotionEffect.class.getConstructor(
                    PotionEffectType.class, int.class, int.class, boolean.class, boolean.class, boolean.class);
            player.addPotionEffect((PotionEffect) ctor.newInstance(
                    PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        } catch (Exception e) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true));
        }
    }

    public void savePlayerData(Player player) {
        try {
            Location loc = player.getLocation();
            db.executeUpdate(
                    "MERGE INTO staff_data (uuid, inventory, armor, level, exp, total_exp, effects, " +
                            "world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    player.getUniqueId().toString(),
                    serializeItemStacks(player.getInventory().getContents()),
                    serializeItemStacks(player.getInventory().getArmorContents()),
                    player.getLevel(), player.getExp(), player.getTotalExperience(),
                    serializeEffects(player.getActivePotionEffects()),
                    loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                    (double) loc.getYaw(), (double) loc.getPitch());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save staff data for " + player.getName(), e);
        }
    }

    public void restorePlayerData(Player player) {
        try {
            ResultSet rs = db.executeQuery("SELECT * FROM staff_data WHERE uuid = ?", player.getUniqueId().toString());
            if (rs.next()) {
                player.getInventory().setContents(deserializeItemStacks(rs.getString("inventory")));
                player.getInventory().setArmorContents(deserializeItemStacks(rs.getString("armor")));
                player.setLevel(rs.getInt("level"));
                player.setExp((float) rs.getDouble("exp"));
                player.setTotalExperience(rs.getInt("total_exp"));
                deserializeEffects(rs.getString("effects")).forEach(e -> player.addPotionEffect(e));

                World world = Bukkit.getWorld(rs.getString("world"));
                if (world != null) {
                    player.teleport(new Location(world,
                            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            (float) rs.getDouble("yaw"), (float) rs.getDouble("pitch")));
                }
                db.executeUpdate("DELETE FROM staff_data WHERE uuid = ?", player.getUniqueId().toString());
            }
            rs.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to restore staff data for " + player.getName(), e);
        }
    }

    public void teleportToRandomPlayer(Player player) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.remove(player);
        if (players.isEmpty()) {
            player.sendMessage("§cNo other players online.");
            return;
        }
        Player target = players.get(new Random().nextInt(players.size()));
        player.teleport(target.getLocation().add(0, 1, 0));
        player.sendMessage("§7Teleported to " + target.getName() + ".");
    }
}
