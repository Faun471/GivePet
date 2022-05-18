package me.faun.givepet.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import mc.obliviate.bloksqliteapi.sqlutils.*;

import me.faun.givepet.configs.Config;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Configs;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.GivePet;
import me.faun.givepet.Request;
import me.faun.givepet.sql.SQLManager;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.SQLUtils;
import me.faun.givepet.utils.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
import java.util.HashMap;

@Command("givepet")
public class GivePetCommand extends BaseCommand {
    private final GivePet plugin = GivePet.getInstance();
    private final SQLManager sqlManager = new SQLManager(GivePet.getInstance());
    private final ConfigManager configManager = new ConfigManager();
    private final SQLTable requestsTable = plugin.getSqlTable();
    private final HashMap<Player, Request> requests = GivePet.requests;

    @Suggestion("players")
    public void defaultCommand(Player sender, String arg) {
        Player receiver = Bukkit.getPlayer(arg);
        if (receiver == null) {
            StringUtils.sendComponent(sender, Messages.PLAYER_NOT_ONLINE);
            return;
        }

        if (receiver == sender) {
            StringUtils.sendComponent(sender, configManager.getStringFromMessages(Messages.CANNOT_TRANSFER_SELF));
            return;
        }

        ResultSet resultSet = requestsTable.select("sender", sender.getUniqueId().toString());
        if (SQLUtils.getStringFromResultSet(resultSet, "finished").equalsIgnoreCase("pending")) {
            StringUtils.sendComponent(sender, configManager.getStringFromMessages(Messages.PENDING_REQUEST));
            return;
        }

        long time = System.currentTimeMillis();
        Request request = new Request(sender.getUniqueId(), receiver.getUniqueId(), time);
        requests.put(receiver, request);

        sqlManager.createRow(requestsTable, new String[]{String.valueOf(sender.getUniqueId()), String.valueOf(receiver.getUniqueId()), String.valueOf(time), "pending"}, "sender");
        StringUtils.sendComponent(sender, configManager.getStringFromMessages(Messages.GIVER_REQUEST_MESSAGE)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

        StringUtils.sendComponent(receiver, configManager.getStringFromMessages(Messages.RECEIVER_REQUEST_MESSAGE)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

        BukkitTask runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (request.getAccepted()) {
                    sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "accepted");
                    requestsTable.delete(sender.getUniqueId().toString());
                    StringUtils.sendComponent(sender, configManager.getStringFromMessages(Messages.GIVER_REQUEST_ACCEPT)
                            .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                            .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

                    PetUtils.addPDC(sender, receiver.getName());
                    cancel();
                    return;
                }

                sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "rejected");
                requestsTable.delete(sender.getUniqueId().toString());
                StringUtils.sendComponent(sender, configManager.getStringFromMessages(Messages.GIVER_REQUEST_REJECT)
                        .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                        .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
                cancel();

            }
        }.runTaskTimerAsynchronously(GivePet.getInstance(), 20L, 20L);

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            if (runnable.isCancelled() || sender.getPersistentDataContainer().isEmpty()) {
                return;
            }

            runnable.cancel();
            sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "expired");
            requestsTable.delete(sender.getUniqueId().toString());

            StringUtils.sendComponent(sender, configManager.getStringFromMessages(Messages.GIVER_REQUEST_EXPIRED)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

            StringUtils.sendComponent(receiver, configManager.getStringFromMessages(Messages.RECEIVER_REQUEST_EXPIRED)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
        }, 20L * ((int) configManager.getConfigValue(Configs.CONFIG, Config.REQUEST_TIME)));
    }

    @SubCommand("help")
    @Suggestion("help")
    public void helpCommand(CommandSender sender) {
        StringUtils.sendComponent(sender, "&cHelp", "&atest?", "&eyellow");
    }

    @SubCommand("reject")
    public void rejectRequest(Player commandSender) {

        if (hasRequest(commandSender)) {
            StringUtils.sendComponent(commandSender, configManager.getStringFromMessages(Messages.NO_PENDING_REQUEST));
            return;
        }

        Request request = requests.get(commandSender);
        request.setAccepted(false);

        StringUtils.sendComponent(commandSender, configManager.getStringFromMessages(Messages.RECEIVER_REQUEST_REJECT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
        requests.remove(commandSender);
    }

    @SubCommand("accept")
    public void acceptRequest(Player commandSender) {
        if (hasRequest(commandSender)) {
            StringUtils.sendComponent(commandSender, configManager.getStringFromMessages(Messages.NO_PENDING_REQUEST));
            return;
        }

        Request request = requests.get(commandSender);
        request.setAccepted(true);

        StringUtils.sendComponent(commandSender, configManager.getStringFromMessages(Messages.RECEIVER_REQUEST_ACCEPT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%giver%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
        requests.remove(commandSender);
    }

    @SubCommand("reload")
    @Suggestion("")
    @Permission("givepet.reload")
    public void reload(CommandSender commandSender) {
        ConfigManager configManager = new ConfigManager();
        configManager.reloadConfigs();
        StringUtils.sendComponent(commandSender, configManager.getStringFromMessages(Messages.RELOAD_SUCCESS));
    }

    private boolean hasRequest(Player commandSender) {
        if (!requests.containsKey(commandSender)) {
            return false;
        }

        ResultSet resultSet = requestsTable.select("receiver", commandSender.getUniqueId().toString());
        return SQLUtils.getStringFromResultSet(resultSet, "receiver").equalsIgnoreCase("null");
    }
}
