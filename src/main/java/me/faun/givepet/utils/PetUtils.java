package me.faun.givepet.utils;

import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.GivePet;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.request.Request;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.HashMap;

public class PetUtils {
    private static final NamespacedKey key = new NamespacedKey(GivePet.getInstance(), "give-pet");

    public static void addPDC(@NotNull Player giver, String arg) {
        PersistentDataContainer pdc = giver.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, arg);

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            if (pdc.isEmpty()) {
                return;
            }

            pdc.remove(key);
            StringUtils.sendComponent(giver, Messages.TAME_EXPIRE);
        }, 20L * 15);
    }

    public static void removePDC(@NotNull Player player) {
        if (player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            player.getPersistentDataContainer().remove(key);
        }
    }

    public static NamespacedKey getKey() {
        return key;
    }

    public static boolean hasRequest(Player player) {
        GivePet plugin = GivePet.getInstance();
        HashMap<Player, Request> requests = GivePet.requests;
        SQLTable requestsTable = plugin.getSqlTable();

        if (requests.containsKey(player)) {
            return true;
        }

        ResultSet receiver = requestsTable.select("receiver", player.getUniqueId().toString());
        return !SQLUtils.getStringFromResultSet(receiver, "receiver").equalsIgnoreCase("null");
    }
}
