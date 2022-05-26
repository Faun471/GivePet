package me.faun.givepet.listeners;

import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.GivePet;
import me.faun.givepet.configs.Config;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Configs;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.events.PetRequestEvent;
import me.faun.givepet.request.Request;
import me.faun.givepet.request.State;
import me.faun.givepet.sql.SQLManager;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class PetRequestListener implements Listener {
    private final GivePet plugin;
    private final SQLTable requestsTable;
    private final SQLManager sqlManager;
    private final ConfigManager configManager;

    public PetRequestListener(GivePet plugin, SQLTable requestsTable, SQLManager sqlManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.requestsTable = requestsTable;
        this.sqlManager = sqlManager;
        this.configManager = configManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPetRequest(PetRequestEvent event) {
        Request request = event.getRequest();
        Player sender = request.getSenderAsPlayer();
        Player receiver = request.getReceiverAsPlayer();

        request.setAccepted(event.getState());
        switch (event.getState()) {
            case ACCEPTED -> {
                sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "accepted");
                requestsTable.delete(request.getSenderAsPlayer().getUniqueId().toString());

                StringUtils.sendComponent(request.getSenderAsPlayer(), StringUtils.getStringFromMessages(Messages.SENDER_REQUEST_ACCEPT)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
                PetUtils.addPDC(request.getSenderAsPlayer(), request.getReceiverAsPlayer().getName());
                StringUtils.sendComponent(request.getReceiverAsPlayer(), StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_ACCEPT)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
            }

            case REJECTED -> {
                sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "rejected");
                requestsTable.delete(request.getSenderAsPlayer().getUniqueId().toString());

                StringUtils.sendComponent(request.getSenderAsPlayer(), StringUtils.getStringFromMessages(Messages.SENDER_REQUEST_REJECT)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                StringUtils.sendComponent(request.getReceiverAsPlayer(), StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_REJECT)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
            }

            case PENDING -> {
                sqlManager.createRow(requestsTable, "sender", String.valueOf(sender.getUniqueId()), String.valueOf(receiver.getUniqueId()), String.valueOf(request.getTime()), "pending");
                StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.SENDER_REQUEST_MESSAGE)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_MESSAGE)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                BukkitTask runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (request.getAccepted() == State.ACCEPTED || request.getAccepted() == State.REJECTED) {
                            cancel();
                        }
                    }
                }.runTaskTimerAsynchronously(plugin, 20L, 20L);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (runnable.isCancelled()) {
                        return;
                    }

                    runnable.cancel();
                    sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "expired");
                    requestsTable.delete(sender.getUniqueId().toString());

                    StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.SENDER_REQUEST_EXPIRED)
                            .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                            .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                    StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_EXPIRED)
                            .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                            .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
                    plugin.getRequests().remove(receiver);
                }, 20L * (int) configManager.getConfigValue(Configs.CONFIG, Config.REQUEST_TIME));
            }

            case FORCED -> {
                sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "forced");
                requestsTable.delete(request.getSenderAsPlayer().getUniqueId().toString());

                StringUtils.sendComponent(request.getSenderAsPlayer(), StringUtils.getStringFromMessages(Messages.SENDER_REQUEST_ACCEPT)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                PetUtils.addPDC(request.getSenderAsPlayer(), request.getReceiverAsPlayer().getName());
            }
        }
    }
}
