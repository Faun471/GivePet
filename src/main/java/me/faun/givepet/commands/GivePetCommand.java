package me.faun.givepet.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.*;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.GivePet;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.events.PetRequestEvent;
import me.faun.givepet.request.Request;
import me.faun.givepet.request.State;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.SQLUtils;
import me.faun.givepet.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.HashMap;

@dev.triumphteam.cmd.core.annotation.Command("givepet")
@Description("The GivePet plugin's main command.")
public class GivePetCommand extends BaseCommand {
    private final GivePet plugin;
    private final SQLTable requestsTable;
    private final HashMap<Player, Request> requests;

    public GivePetCommand(GivePet plugin, SQLTable requestsTable, HashMap<Player, Request> requests) {
        this.plugin = plugin;
        this.requestsTable = requestsTable;
        this.requests = requests;
    }

    @SubCommand("request")
    @Description("Send a request to another player.")
    @Usage("/givepet request [player]")
    public void request(Player sender, Player receiver) {
        if (receiver == sender) {
            StringUtils.sendComponent(sender, Messages.CANNOT_TRANSFER_SELF);
            return;
        }

        if (PetUtils.hasRequest(receiver)) {
            StringUtils.sendComponent(sender, Messages.RECEIVER_PENDING_REQUEST);
            return;
        }

        ResultSet resultSet = requestsTable.select("sender", sender.getUniqueId().toString());
        if (SQLUtils.getStringFromResultSet(resultSet, "finished").equalsIgnoreCase("pending")) {
            StringUtils.sendComponent(sender, Messages.SENDER_PENDING_REQUEST);
            return;
        }

        Request request = new Request(sender.getUniqueId(), receiver.getUniqueId(), System.currentTimeMillis());
        requests.put(receiver, request);

        Bukkit.getPluginManager().callEvent(new PetRequestEvent(requests.get(receiver), State.PENDING));
    }

    @SubCommand("help")
    @Description("Do you seriously need to know this command's description?")
    @Usage("/givepet help <command>")
    public void help(CommandSender sender, @Suggestion("#help") @Optional String arg) {
        HashMap<String, Command> commands = CommandManager.getCommands();

        if (!commands.containsKey(arg)) {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_HEADER));
            commands.values().stream().filter((command -> CommandManager.hasPermission(sender, command)))
                    .forEach((command) -> StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_COMMAND)
                            .replace("%command%", command.name())
                            .replace("%description%", command.description())
                            .replace("%usage%", command.usage())));
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_FOOTER));
            return;
        }

        Command command = commands.get(arg);
        if (CommandManager.hasPermission(sender, command)) {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.HELP_COMMAND)
                    .replace("%command%", command.name())
                    .replace("%description%", command.description())
                    .replace("%usage%", command.usage()));
        }
    }

    @SubCommand("reject")
    @Description("Reject an incoming request.")
    @Usage("/givepet reject")
    @Requirement(value = "has.request", messageKey = "no.pending.request")
    public void reject(Player player) {
        Bukkit.getPluginManager().callEvent(new PetRequestEvent(requests.get(player), State.REJECTED));
        requests.remove(player);
    }

    @SubCommand("accept")
    @Description("Accept an incoming request.")
    @Usage("/givepet accept")
    @Requirement(value = "has.request", messageKey = "no.pending.request")
    public void accept(Player player) {
        Bukkit.getPluginManager().callEvent(new PetRequestEvent(requests.get(player), State.ACCEPTED));
        requests.remove(player);
    }

    @SubCommand("reload")
    @Permission("givepet.reload")
    @Description("Reloads the plugin.")
    @Usage("/givepet reload")
    public void reload(CommandSender commandSender) {
        ConfigManager configManager = new ConfigManager(plugin);
        double time = System.currentTimeMillis();
        configManager.reloadConfigs();
        StringUtils.sendComponent(commandSender, StringUtils.getStringFromMessages(Messages.RELOAD_SUCCESS)
                .replace("%time%", String.valueOf(System.currentTimeMillis() - time)));
    }

    @SubCommand("force")
    @Permission("givepet.force")
    @Description("Forcefully transfers someone's pet to someone else.")
    @Usage("/givepet force [player]")
    public void force(Player sender, Player receiver) {
        if (receiver == null) {
            StringUtils.sendComponent(sender, Messages.PLAYER_NOT_ONLINE);
            return;
        }

        if (receiver == sender) {
            StringUtils.sendComponent(sender, Messages.CANNOT_TRANSFER_SELF);
            return;
        }

        Request request = new Request(sender.getUniqueId(), receiver.getUniqueId(), System.currentTimeMillis());
        requests.put(receiver, request);
        Bukkit.getPluginManager().callEvent(new PetRequestEvent(requests.get(receiver), State.FORCED));
    }
}
