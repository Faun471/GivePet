package me.faun.givepet;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.suggestion.SuggestionContext;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionResolver;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.commands.GivePetCommand;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.listeners.PlayerInteractListener;
import me.faun.givepet.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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

        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);
        commandManager.registerSuggestion(SuggestionKey.of("players"), (sender, context) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        commandManager.registerSuggestion(SuggestionKey.of("help"), (sender, context) -> List.of("accept", "help", "reject", "reload"));
        commandManager.registerCommand(new GivePetCommand());

        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(),this);
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
