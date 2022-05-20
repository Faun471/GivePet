package me.faun.givepet;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.message.MessageKey;
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
import me.faun.givepet.listeners.PlayerInteractListener;
import me.faun.givepet.request.Request;
import me.faun.givepet.sql.SQLManager;
import me.faun.givepet.utils.PetUtils;
import me.faun.givepet.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public final class GivePet extends JavaPlugin {

    private static GivePet instance;
    private static SQLTable requestsTable;
    private static SQLTable logsTable;
    public static HashMap<Player, Request> requests = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager configManager = new ConfigManager();
        configManager.reloadConfigs();

        SQLManager sqlManager = new SQLManager(this);
        requestsTable = sqlManager.createRequestsTable();
        logsTable = sqlManager.createLogsTable();
        sqlManager.clearTable(requestsTable);

        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new PetRequestListener(), this);

        BukkitCommandManager<CommandSender> bukkitCommandManager = BukkitCommandManager.create(this);
        bukkitCommandManager.registerRequirement(RequirementKey.of("has.request"), (sender) -> PetUtils.hasRequest((Player) sender));

        bukkitCommandManager.registerSuggestion(SuggestionKey.of("#players"), (sender, context) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        bukkitCommandManager.registerSuggestion(SuggestionKey.of("#help"), (sender, context) -> CommandManager.getCommands().values().stream().filter((command -> CommandManager.hasPermission(sender, command))).map((Command::name)).collect(Collectors.toList()));

        bukkitCommandManager.registerMessage(MessageKey.of("no.pending.request", MessageContext.class), ((sender, context) -> StringUtils.sendComponent(sender, Messages.NO_PENDING_REQUEST)));
        bukkitCommandManager.registerMessage(MessageKey.of("has.pending.request", MessageContext.class), ((sender, context) -> StringUtils.sendComponent(sender, Messages.SENDER_PENDING_REQUEST)));
        bukkitCommandManager.registerMessage(MessageKey.UNKNOWN_COMMAND, ((sender, context) -> StringUtils.sendComponent(sender, Messages.UNKNOWN_COMMAND)));
        bukkitCommandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, ((sender, context) -> StringUtils.sendComponent(sender, Messages.NO_PERMISSION)));
        bukkitCommandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, ((sender, context) -> StringUtils.sendComponent(sender, Messages.NO_PERMISSION)));

        bukkitCommandManager.registerCommand(new GivePetCommand());
    }

    public static GivePet getInstance() {
        return instance;
    }

    public SQLTable getSqlTable() {
        return requestsTable;
    }

    public SQLTable getLogsTable() {
        return logsTable;
    }
}
