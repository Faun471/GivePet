package me.faun.givepet.listeners;

import me.faun.givepet.configs.Messages;
import me.faun.givepet.events.PetTransferEvent;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteractListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEntityEvent event) {
        Player giver = event.getPlayer();

        if (!(event.getRightClicked() instanceof Tameable pet)) {
            return;
        }

        if (!giver.getPersistentDataContainer().has(PetUtils.getKey(), PersistentDataType.STRING)) {
            return;
        }

        if (pet.isTamed() && pet.getOwnerUniqueId() != null && !pet.getOwnerUniqueId().equals(giver.getUniqueId())) {
            PetUtils.removePDC(giver);
            StringUtils.sendComponent(giver, StringUtils.getStringFromMessages(Messages.NOT_YOUR_PET));
        }

        Player receiver = Bukkit.getPlayer(giver.getPersistentDataContainer().get(PetUtils.getKey(), PersistentDataType.STRING));
        if (receiver == null) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new PetTransferEvent(pet, giver, receiver));
    }
}
