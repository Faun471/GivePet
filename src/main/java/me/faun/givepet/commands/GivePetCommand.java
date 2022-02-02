package me.faun.givepet.commands;

import mc.obliviate.bloksqliteapi.sqlutils.*;
import me.faun.givepet.configs.Config;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.GivePet;
import me.faun.givepet.Request;
import me.faun.givepet.sql.SQLManager;
import me.faun.givepet.utils.SQLUtils;
import me.faun.givepet.utils.StringUtils;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.NoPermission;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;

@Command("givepet")
public class GivePetCommand extends CommandBase implements Listener {
    SQLManager sqlManager = new SQLManager(GivePet.getInstance());
    ConfigManager configManager = new ConfigManager();
    GivePet plugin = GivePet.getInstance();
    SQLTable requestsTable = plugin.getSqlTable();
    Request request;

    @Default
    @Completion("#players")
    public void defaultCommand(Player sender, String arg) {
        Player receiver = Bukkit.getPlayer(arg);
        if (receiver == null) {
            StringUtils.sendComponent(sender, Messages.PLAYER_NOT_ONLINE);
            return;
        }

        if (receiver == sender) {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.CANNOT_TRANSFER_SELF));
            return;
        }

        ResultSet resultSet = requestsTable.select("sender", sender.getUniqueId().toString());
        if (SQLUtils.getStringFromResultSet(resultSet, "finished").equalsIgnoreCase("pending")) {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.PENDING_REQUEST));
            return;
        }

        String time = StringUtils.unixToDate(System.currentTimeMillis());
        request = new Request(sender.getUniqueId(), receiver.getUniqueId(), time);

        sqlManager.createRow(requestsTable, new String[]{String.valueOf(sender.getUniqueId()), String.valueOf(receiver.getUniqueId()), time, "pending"}, "sender");
        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_MESSAGE)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

        StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_MESSAGE)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

        BukkitTask runnable = new BukkitRunnable() {
            @Override
            public void run() {
                switch (request.getAccepted().toLowerCase()) {
                    case "accepted" -> {
                        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "accepted");
                        requestsTable.delete(sender.getUniqueId().toString());
                        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_ACCEPT)
                                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                        GivePet.getInstance().getPetManager().addPDC(sender, receiver.getName());
                        cancel();
                    }

                    case "rejected" -> {
                        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "rejected");
                        requestsTable.delete(sender.getUniqueId().toString());
                        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_REJECT)
                                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                        cancel();
                    }
                }
            }
        }.runTaskTimerAsynchronously(GivePet.getInstance(), 20L, 20L);

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            if (runnable.isCancelled() || sender.getPersistentDataContainer().isEmpty()) {
                return;
            }

            runnable.cancel();
            sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "expired");
            requestsTable.delete(sender.getUniqueId().toString());

            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_EXPIRED)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

            StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_EXPIRED)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
        }, 20L * ((int) configManager.getConfigValue("config", Config.REQUEST_TIME)));
    }

    // TODO: 2/2/2022 Add Help Commands
    @SubCommand("help")
    public void helpCommand(CommandSender sender) {
        StringUtils.sendComponent(sender, "&cHelp command not implemented yet.");
    }


    @SubCommand("reject")
    public void rejectRequest(Player commandSender) {
        if (hasRequest(commandSender)) {
            StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.NO_PENDING_REQUEST)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
            return;
        }

        request.setAccepted("rejected");
        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_REJECT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
    }

    @SubCommand("accept")
    public void acceptRequest(Player commandSender) {
        if (hasRequest(commandSender)) {
            StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.NO_PENDING_REQUEST)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
            return;
        }

        request.setAccepted("accepted");
        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_ACCEPT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
    }

    @SubCommand("reload")
    @Permission("givepet.reload")
    @NoPermission("#cmd.no.permission")
    public void reload(CommandSender commandSender) {
        ConfigManager configManager = new ConfigManager();
        configManager.reloadConfigs();
        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RELOAD_SUCCESS));
    }

    private boolean hasRequest(Player commandSender) {
        ResultSet resultSet = requestsTable.select("receiver", commandSender.getUniqueId().toString());
        return SQLUtils.getStringFromResultSet(resultSet, "receiver").equalsIgnoreCase("null");
    }
}
