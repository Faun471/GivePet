package me.faun.givepet;

import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.Commands.GivePetCommand;
import me.faun.givepet.Listeners.PlayerInteractListener;
import me.faun.givepet.SQL.SQLManager;
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

        SQLManager sqlManager = new SQLManager(this);
        requestsTable = sqlManager.createRequestsTable();
        logsTable = sqlManager.createLogsTable();

        CommandManager commandManager = new CommandManager(this);
        commandManager.register(new GivePetCommand());

        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(),this);
        sqlManager.clearTable(requestsTable);
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
