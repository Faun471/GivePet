package me.faun.givepet.Listeners;

import me.faun.givepet.PetManager;
import me.faun.givepet.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteractListener implements Listener {

    PetManager petManager = new PetManager();

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!(event.getRightClicked() instanceof Tameable pet)) {
            return;
        }

        if (!player.getPersistentDataContainer().has(petManager.getKey(), PersistentDataType.STRING)) {
            return;
        }

        if (pet.getOwnerUniqueId() != null && !pet.getOwnerUniqueId().equals(player.getUniqueId())) {
            petManager.removePDC(player, petManager.getKey());
            StringUtils.sendComponent(player, "&cThat's not your pet!");
        }

        if (player.getPersistentDataContainer().get(petManager.getKey(),PersistentDataType.STRING) == null) {
            StringUtils.sendComponent(player, "&PDC is null!");
            return;
        }

        Player receiver = Bukkit.getPlayer(player.getPersistentDataContainer().get(petManager.getKey(),PersistentDataType.STRING));
        if (receiver == null) {
            StringUtils.sendComponent(player, "&cReceiver is null!");
            return;
        }

        transferPet(pet, player, receiver);
    }

    public void transferPet(Tameable pet, Player giver, Player receiver) {
        pet.setOwner(receiver);
        petManager.removePDC(giver, petManager.getKey());

        StringUtils.sendComponent(giver, "Successfully gave your pet to " + receiver.name());
        StringUtils.sendComponent(receiver, giver.name() + " gave you their pet.");
        petManager.removePDC(giver, petManager.getKey());
    }

}
