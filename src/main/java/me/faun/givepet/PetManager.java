package me.faun.givepet;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PetManager {

    private final NamespacedKey giveKey = new NamespacedKey(GivePet.getInstance(),"give-pet");
    private final NamespacedKey requestKey = new NamespacedKey(GivePet.getInstance(), "request");

    public PetManager() {

    }

    public NamespacedKey getGiveKey() {
        return giveKey;
    }

    public NamespacedKey getRequestKey() {
        return requestKey;
    }

    public void addPDC(Player giver, String arg) {
        PersistentDataContainer pdc = giver.getPersistentDataContainer();
        pdc.set(giveKey, PersistentDataType.STRING, arg);

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            pdc.remove(giveKey);
            giver.sendMessage("timer expired.");
        }, 20L * 15);
    }

    public void removePDC(Player player, NamespacedKey key) {
        if (player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            player.getPersistentDataContainer().remove(key);
        }
    }
}
