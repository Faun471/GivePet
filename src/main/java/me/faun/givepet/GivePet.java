package me.faun.givepet;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.message.context.DefaultMessageContext;
import dev.triumphteam.cmd.core.message.context.MessageContext;
import dev.triumphteam.cmd.core.requirement.RequirementKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.commands.Command;
import me.faun.givepet.commands.CommandManager;
import me.faun.givepet.commands.GivePetCommand;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.listeners.PetRequestListener;
import me.faun.givepet.listeners.PetTransferListener;
import me.faun.givepet.listeners.PlayerInteractListener;
import me.faun.givepet.request.Request;
import me.faun.givepet.sql.SQLManager;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.stream.Collectors;

public final class GivePet extends JavaPlugin {
    private static SQLTable requestsTable;
    private static SQLTable logsTable;
    public static HashMap<Player, Request> requests = new HashMap<>();

    @Override
    public void onEnable() {
        SQLManager sqlManager = new SQLManager(this);
        requestsTable = sqlManager.createRequestsTable();
        logsTable = sqlManager.createLogsTable();

        ConfigManager configManager = new ConfigManager(this);
        sqlManager.clearTable(requestsTable);

        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new PetRequestListener(this, requestsTable, sqlManager, configManager), this);
        Bukkit.getPluginManager().registerEvents(new PetTransferListener(this, configManager), this);

        BukkitCommandManager<CommandSender> bukkitCommandManager = BukkitCommandManager.create(this);
        bukkitCommandManager.registerRequirement(RequirementKey.of("has.request"), sender -> PetUtils.hasRequest((Player) sender));

        bukkitCommandManager.registerSuggestion(SuggestionKey.of("#help"), (sender, context) -> CommandManager.getCommands().values().stream()
                .filter((command -> CommandManager.hasPermission(sender, command)))
                .map((Command::name))
                .collect(Collectors.toList()));

        bukkitCommandManager.registerMessage(MessageKey.of("no.pending.request", MessageContext.class), ((sender, context) -> StringUtils.sendComponent(sender, Messages.NO_PENDING_REQUEST)));
        bukkitCommandManager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> StringUtils.sendComponent(sender, Messages.UNKNOWN_COMMAND));
        bukkitCommandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> StringUtils.sendComponent(sender, Messages.NO_PERMISSION));
        bukkitCommandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, GivePet::sendHelp);
        bukkitCommandManager.registerMessage(MessageKey.TOO_MANY_ARGUMENTS, GivePet::sendHelp);
        bukkitCommandManager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, context) -> {
            if (context.getArgumentType() == Player.class) {
                StringUtils.sendComponent(sender, Messages.PLAYER_NOT_ONLINE);
            }
        });

        GivePetCommand givePetCommand = new GivePetCommand(this, requestsTable, requests);
        bukkitCommandManager.registerCommand(givePetCommand);
    }

    public SQLTable getSqlTable() {
        return requestsTable;
    }

    public SQLTable getLogsTable() {
        return logsTable;
    }

    private static void sendHelp(@NotNull CommandSender sender, @NotNull DefaultMessageContext context) {
        Command command = CommandManager.getCommands().getOrDefault(context.getSubCommand(), null);

        if (command == null) {
            Bukkit.dispatchCommand(sender, "givepet help");
            return;
        }

        StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.INVALID_ARGUMENT)
                .replace("%command%", command.name())
                .replace("%description%", command.description())
                .replace("%usage%", command.usage()));
    }
}
