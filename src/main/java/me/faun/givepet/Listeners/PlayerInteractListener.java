package me.faun.givepet.Listeners;

import me.faun.givepet.Configs.Messages;
import me.faun.givepet.Events.PetTransferEvent;
import me.faun.givepet.PetManager;
import me.faun.givepet.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
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

        if (pet.isTamed() && pet.getOwnerUniqueId() != null && !pet.getOwnerUniqueId().equals(player.getUniqueId())) {
            petManager.removePDC(player, petManager.getKey());
            StringUtils.sendComponent(player, StringUtils.getStringFromMessages(Messages.NOT_YOUR_PET));
        }

        if (player.getPersistentDataContainer().get(petManager.getKey(),PersistentDataType.STRING) == null) {
            System.out.println("PDC is null.");
            return;
        }

        Player receiver = Bukkit.getPlayer(player.getPersistentDataContainer().get(petManager.getKey(),PersistentDataType.STRING));
        if (receiver == null) {
            System.out.println("Receiver is null.");
            return;
        }

        Bukkit.getServer().getPluginManager().callEvent(new PetTransferEvent());
        transferPet(pet, player, receiver);
    }

    public void transferPet(Tameable pet, Player giver, Player receiver) {
        if (pet instanceof Sittable sittable && sittable.isSitting()) {
            sittable.setSitting(false);
        }

        pet.setOwner(receiver);
        petManager.removePDC(giver, petManager.getKey());

        StringUtils.sendComponent(giver, StringUtils.getStringFromMessages(Messages.GIVE_PET_SUCCESS));
        StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVE_PET_SUCCESS));
        petManager.removePDC(giver, petManager.getKey());
    }

}
