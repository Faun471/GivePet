package me.faun.givepet;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PetManager {

    private final NamespacedKey key = new NamespacedKey(GivePet.getInstance(),"give-pet");

    public PetManager() {

    }

    public NamespacedKey getKey() {
        return key;
    }

    public void addPDC(Player giver, String arg) {
        PersistentDataContainer pdc = giver.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, arg);

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            if (pdc.isEmpty()) {
                return;
            }

            pdc.remove(key);
            giver.sendMessage("timer expired.");
        }, 20L * 15);
    }

    public void removePDC(Player player, NamespacedKey key) {
        if (player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            player.getPersistentDataContainer().remove(key);
        }
    }
}
