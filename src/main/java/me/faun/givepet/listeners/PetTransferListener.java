package me.faun.givepet.listeners;

import me.faun.givepet.GivePet;
import me.faun.givepet.configs.Config;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Configs;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.events.PetTransferEvent;
import me.faun.givepet.request.Request;
import me.faun.givepet.request.State;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PetTransferListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPetTransfer(PetTransferEvent event) {
        Player giver = event.getSender();
        Player receiver = event.getReceiver();
        Tameable pet = event.getPet();

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

        Request request = GivePet.requests.get(receiver);

        StringUtils.sendComponent(giver, StringUtils.getStringFromMessages(Messages.GIVE_PET_SUCCESS)
                .replace("%receiver%", StringUtils.componentToString(receiver.displayName()))
                .replace("%sender%", StringUtils.componentToString(giver.displayName())));

        if (request != null && request.getAccepted() == State.FORCED) {
            return;
        }

        StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVE_PET_SUCCESS)
                .replace("%receiver%", StringUtils.componentToString(receiver.displayName()))
                .replace("%sender%", StringUtils.componentToString(giver.displayName())));
    }
}
