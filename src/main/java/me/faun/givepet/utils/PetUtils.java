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
    private static final GivePet plugin = GivePet.getPlugin(GivePet.class);
    private static final NamespacedKey key = new NamespacedKey(plugin, "give-pet");

    /**
     * This will add a pdc to the player.
     *
     * @param giver the player to add a pdc to.
     * @param arg the value of the pdc that will be added to the player.
     */
    public static void addPDC(@NotNull Player giver, String arg) {
        PersistentDataContainer pdc = giver.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, arg);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pdc.isEmpty()) {
                return;
            }

            pdc.remove(key);
            StringUtils.sendComponent(giver, Messages.TAME_EXPIRE);
        }, 20L * 15);
    }

    /**
     * This will remove all the pdc attached on the player.
     *
     * @param player the player that will have their pdc removed.
     */
    public static void removePDC(@NotNull Player player) {
        if (player.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            player.getPersistentDataContainer().remove(key);
        }
    }

    /**
     * @return the plugin's NameSpacedKey.
     */
    public static NamespacedKey getKey() {
        return key;
    }

    /**
     * This will check whether a player has an active request with another
     * player or not.
     *
     * @param player the player to be checked.
     * @return whether the player has an active request or not.
     */
    public static boolean hasRequest(Player player, HashMap<Player, Request> requests) {
        SQLTable requestsTable = plugin.getSqlTable();

        if (requests.containsKey(player)) {
            return true;
        }

        ResultSet receiver = requestsTable.select("receiver", player.getUniqueId().toString());
        if (!SQLUtils.getStringFromResultSet(receiver, "receiver").equalsIgnoreCase("null")) {
            return true;
        }

        ResultSet sender = requestsTable.select("sender", player.getUniqueId().toString());
        return !SQLUtils.getStringFromResultSet(sender, "sender").equalsIgnoreCase("null");
    }
}
