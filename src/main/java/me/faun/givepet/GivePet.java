package me.faun.givepet;

import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.Commands.GivePetCommand;
import me.faun.givepet.Configs.ConfigManager;
import me.faun.givepet.Configs.Messages;
import me.faun.givepet.Listeners.PlayerInteractListener;
import me.faun.givepet.SQL.SQLManager;
import me.faun.givepet.Utils.StringUtils;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GivePet extends JavaPlugin {

    private static GivePet instance;
    private static SQLTable requestsTable;
    private static SQLTable logsTable;

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager configManager = new ConfigManager();
        configManager.reloadConfigs();

        SQLManager sqlManager = new SQLManager(this);
        requestsTable = sqlManager.createRequestsTable();
        logsTable = sqlManager.createLogsTable();
        sqlManager.clearTable(requestsTable);

        CommandManager commandManager = new CommandManager(this, true);
        commandManager.getMessageHandler().register("#cmd.no.permission", sender -> {
            StringUtils.sendComponent(sender, StringUtils.getStringFromMessages(Messages.NO_PERMISSION));
        });
        commandManager.register(new GivePetCommand());



        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(),this);
    }

    @Override
    public void onDisable() {
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
