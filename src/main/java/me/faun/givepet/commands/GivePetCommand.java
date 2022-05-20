package me.faun.givepet.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Description;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.GivePet;
import me.faun.givepet.request.Request;
import me.faun.givepet.configs.Config;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Configs;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.request.State;
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

@dev.triumphteam.cmd.core.annotation.Command("givepet")
@Description("The GivePet plugin's main command.")
public
class GivePetCommand extends BaseCommand {
    private final GivePet plugin = GivePet.getInstance();
    private final SQLManager sqlManager = new SQLManager(GivePet.getInstance());
    private final ConfigManager configManager = new ConfigManager();
    private final SQLTable requestsTable = plugin.getSqlTable();
    private final HashMap<Player, Request> requests = GivePet.requests;

    @SubCommand("request")
    @Description("Send a request to another player.")
    public void requestCommand(Player sender, @Suggestion("#players") Player receiver) {
        if (receiver == null) {
            StringUtils.sendComponent(sender, Messages.PLAYER_NOT_ONLINE);
            return;
        }

        if (receiver == sender) {
            StringUtils.sendComponent(sender, Messages.CANNOT_TRANSFER_SELF);
            return;
        }

        ResultSet resultSet = requestsTable.select("sender", sender.getUniqueId().toString());
        if (SQLUtils.getStringFromResultSet(resultSet, "finished").equalsIgnoreCase("pending")) {
            StringUtils.sendComponent(sender, Messages.PENDING_REQUEST);
            return;
        }

        Request request = new Request(sender.getUniqueId(), receiver.getUniqueId(), System.currentTimeMillis());
        requests.put(receiver, request);

        sqlManager.createRow(requestsTable, new String[]{String.valueOf(sender.getUniqueId()), String.valueOf(receiver.getUniqueId()), String.valueOf(request.getTime()), "pending"}, "sender");
        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_MESSAGE)
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
        }.runTaskTimerAsynchronously(GivePet.getInstance(), 20L, 20L * ((int) configManager.getConfigValue(Configs.CONFIG, Config.REQUEST_TIME)));

        Bukkit.getScheduler().runTaskLater(GivePet.getInstance(), () -> {
            if (runnable.isCancelled() || sender.getPersistentDataContainer().isEmpty()) {
                return;
            }

            runnable.cancel();
            sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "expired");
            requestsTable.delete(sender.getUniqueId().toString());

            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_EXPIRED)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

            StringUtils.sendComponent(receiver, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_EXPIRED)
                    .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                    .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
        }, 20L * ((int) configManager.getConfigValue(Configs.CONFIG, Config.REQUEST_TIME)));
    }

    @SubCommand("help")
    @Description("Do you seriously need to know this command's description?")
    public void helpCommand(CommandSender sender, @Optional String arg) {
        HashMap<String, Command> commands = CommandManager.getCommands();

        if (commands.containsKey(arg)) {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_COMMAND)
                    .replace("%command%", arg)
                    .replace("%description%", commands.get(arg).description()));
            return;
        }

        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_HEADER));
        for (String command : commands.keySet()) {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_COMMAND)
                    .replace("%command%", command)
                    .replace("%description%", commands.get(command).description()));
        }
        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_FOOTER));

    }

    @SubCommand("reject")
    @Description("Reject an incoming request.")
    public void rejectRequest(Player commandSender) {
        if (!hasRequest(commandSender)) {
            StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.NO_PENDING_REQUEST));
            return;
        }

        Request request = requests.get(commandSender);
        request.setAccepted(State.REJECTED);

        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "rejected");
        requestsTable.delete(request.getSenderAsPlayer().getUniqueId().toString());
        StringUtils.sendComponent(request.getSenderAsPlayer(), StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_REJECT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

        StringUtils.sendComponent(request.getSenderAsPlayer(), StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_REJECT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_REJECT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
        requests.remove(commandSender);
    }

    @SubCommand("accept")
    @Description("Accept an incoming request.")
    public void acceptRequest(Player player) {
        if (!hasRequest(player)) {
            StringUtils.sendComponent(player, StringUtils.getStringFromMessages(Messages.NO_PENDING_REQUEST));
            return;
        }

        Request request = requests.get(player);
        request.setAccepted(State.ACCEPTED);

        sqlManager.logRequest(request.getSender().toString(), request.getReceiver().toString(), request.getTime(), "accepted");
        requestsTable.delete(request.getSenderAsPlayer().getUniqueId().toString());
        StringUtils.sendComponent(request.getSenderAsPlayer(), StringUtils.getStringFromMessages(Messages.GIVER_REQUEST_ACCEPT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));

        PetUtils.addPDC(request.getSenderAsPlayer(), request.getReceiverAsPlayer().getName());



        StringUtils.sendComponent(player, StringUtils.getStringFromMessages(Messages.RECEIVER_REQUEST_ACCEPT)
                .replace("%receiver%", StringUtils.componentToString(request.getReceiverAsPlayer().displayName()))
                .replace("%sender%", StringUtils.componentToString(request.getSenderAsPlayer().displayName())));
        requests.remove(player);
    }

    @SubCommand("reload")
    @Permission("givepet.reload")
    @Description("Reloads the plugin.")
    public void reload(CommandSender commandSender) {
        ConfigManager configManager = new ConfigManager();
        configManager.reloadConfigs();
        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RELOAD_SUCCESS));
    }

    private boolean hasRequest(Player commandSender) {
        if (!requests.containsKey(commandSender)) {
            return false;
        }

        ResultSet resultSet = requestsTable.select("receiver", commandSender.getUniqueId().toString());
        return !SQLUtils.getStringFromResultSet(resultSet, "receiver").equalsIgnoreCase("null");
    }
}
