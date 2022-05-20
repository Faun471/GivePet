package me.faun.givepet.listeners;

import me.faun.givepet.configs.Config;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Configs;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.events.PetTransferEvent;
import me.faun.givepet.GivePet;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEntityEvent event) {
        ConfigManager configManager = new ConfigManager();
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

        PetTransferEvent petTransferEvent = new PetTransferEvent(pet, giver, receiver);
        Bukkit.getPluginManager().callEvent(petTransferEvent);
        if (petTransferEvent.isCancelled()) {
            return;
        }

        transferPet(pet, giver, receiver);
    }

    private void transferPet(Tameable pet, Player giver, Player receiver) {
        ConfigManager configManager = new ConfigManager();

        if ((boolean) configManager.getConfigValue(Configs.CONFIG, Config.PET_STAND) && (pet instanceof Sittable sittable && sittable.isSitting())) {
            sittable.setSitting(false);
        }

        if ((boolean) configManager.getConfigValue(Configs.CONFIG, Config.PET_TELEPORT)) {
            pet.teleport(receiver);
        }

        pet.setInvulnerable(true);

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> pet.setInvulnerable(false), 20L * ((int) configManager.getConfigValue(Configs.CONFIG, Config.PET_INVINCIBILITY_TIME)));

        PetUtils.removePDC(giver);
        pet.setOwner(receiver);

        StringUtils.sendComponent(giver, StringUtils.getStringFromMessages(Messages.GIVE_PET_SUCCESS)
                .replace("%receiver%", StringUtils.componentToString(receiver.displayName()))
                .replace("%sender%", StringUtils.componentToString(giver.displayName())));

        StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVE_PET_SUCCESS)
                .replace("%receiver%", StringUtils.componentToString(receiver.displayName()))
                .replace("%sender%", StringUtils.componentToString(giver.displayName())));

    }

}
