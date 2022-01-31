package me.faun.givepet.Listeners;

import me.faun.givepet.PetManager;
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

        if (!player.getPersistentDataContainer().has(petManager.getGiveKey(), PersistentDataType.STRING)) {
            player.sendMessage("is not transferring.");
            return;
        }

        if (!(event.getRightClicked() instanceof Tameable)) {
            petManager.removePDC(player, petManager.getGiveKey());
            System.out.println("not tamable");
        }

        Tameable pet = (Tameable) event.getRightClicked();
        if (!pet.getOwnerUniqueId().equals(player.getUniqueId())) {
            petManager.removePDC(player, petManager.getGiveKey());
            player.sendMessage("That's not your pet, dum dum.");
        }

        Player receiver = Bukkit.getPlayer(player.getPersistentDataContainer().get(petManager.getGiveKey(),PersistentDataType.STRING));
        transferPet(pet, player, receiver);
        petManager.removePDC(player, petManager.getGiveKey());
    }

    public void transferPet(Tameable pet, Player giver, Player receiver) {
        pet.setOwner(receiver);
        petManager.removePDC(giver, petManager.getGiveKey());
        petManager.removePDC(giver, petManager.getRequestKey());

        giver.sendMessage("Successfully gave your pet to " + receiver.getDisplayName());
        receiver.sendMessage(giver.getDisplayName() + " gave you their pet.");
        petManager.removePDC(receiver, petManager.getRequestKey());
    }

}
