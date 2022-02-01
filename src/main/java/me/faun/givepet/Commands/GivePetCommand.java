package me.faun.givepet.Commands;

import mc.obliviate.bloksqliteapi.sqlutils.*;
import me.faun.givepet.Configs.ConfigManager;
import me.faun.givepet.Configs.Messages;
import me.faun.givepet.GivePet;
import me.faun.givepet.PetManager;
import me.faun.givepet.Request;
import me.faun.givepet.SQL.SQLManager;
import me.faun.givepet.Utils.SQLUtils;
import me.faun.givepet.Utils.StringUtils;
import me.mattstudios.mf.annotations.*;
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
    PetManager petManager = new PetManager();
    SQLManager sqlManager = new SQLManager(GivePet.getInstance());
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

        String time = StringUtils.unixToDate(System.currentTimeMillis());
        request = new Request(sender.getUniqueId(), receiver.getUniqueId(), time);

        if (SQLUtils.getStringFromResultSet(resultSet, "finished").equalsIgnoreCase("pending")) {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.PENDING_REQUEST));
            return;
        }

        sqlManager.createRow(requestsTable, new String[]{String.valueOf(sender.getUniqueId()), String.valueOf(receiver.getUniqueId()), time, "pending"}, "sender");
        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_MESSAGE));
        StringUtils.sendComponent(receiver, "&e" + StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_MESSAGE));

        BukkitTask runnable = new BukkitRunnable() {
            @Override
            public void run() {
                switch (request.getAccepted().toLowerCase()) {
                    case "accepted" -> {
                        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "accepted");
                        requestsTable.delete(sender.getUniqueId().toString());
                        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_ACCEPT));
                        petManager.addPDC(sender, receiver.getName());
                        cancel();
                    }
                    case "rejected" -> {
                        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "rejected");
                        requestsTable.delete(sender.getUniqueId().toString());
                        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_REJECT));
                        cancel();
                    }
                }
            }
        }.runTaskTimerAsynchronously(GivePet.getInstance(), 20L, 20L);

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            if (runnable.isCancelled()) {
                return;
            }

            runnable.cancel();
            sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "expired");
            requestsTable.delete(sender.getUniqueId().toString());

            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_EXPIRED));
            StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_EXPIRED));
        }, 20L * 15);
    }

    @SubCommand("help")
    public void helpCommand(CommandSender sender) {
        StringUtils.sendComponent(sender, "&cPlz halp me!");
    }

    @SubCommand("reject")
    public void rejectRequest(Player commandSender) {
        if (hasRequest(commandSender)) {
            StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.NO_PENDING_REQUEST));
            return;
        }

        request.setAccepted("rejected");
        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_REJECT));
    }

    @SubCommand("accept")
    public void acceptRequest(Player commandSender) {
        if (hasRequest(commandSender)) {
            StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.NO_PENDING_REQUEST));
            return;
        }

        request.setAccepted("accepted");
        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_ACCEPT));
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
