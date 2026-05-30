package com.modplugin.managers;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public final class SerializationUtil {

    private SerializationUtil() {}

    public static String serializeItemStacks(ItemStack[] items) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);
        oos.writeInt(items.length);
        for (ItemStack item : items) {
            oos.writeObject(item);
        }
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static ItemStack[] deserializeItemStacks(String data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);
        int size = ois.readInt();
        ItemStack[] items = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            items[i] = (ItemStack) ois.readObject();
        }
        ois.close();
        return items;
    }

    public static String serializeEffects(Collection<PotionEffect> effects) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos);
        oos.writeInt(effects.size());
        for (PotionEffect effect : effects) {
            oos.writeObject(effect);
        }
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static Collection<PotionEffect> deserializeEffects(String data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream ois = new BukkitObjectInputStream(bais);
        int size = ois.readInt();
        List<PotionEffect> effects = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            effects.add((PotionEffect) ois.readObject());
        }
        ois.close();
        return effects;
    }
}
