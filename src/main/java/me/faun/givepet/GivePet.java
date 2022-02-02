package me.faun.givepet;

import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.commands.GivePetCommand;
import me.faun.givepet.configs.ConfigManager;
import me.faun.givepet.configs.Messages;
import me.faun.givepet.listeners.PlayerInteractListener;
import me.faun.givepet.sql.SQLManager;
import me.faun.givepet.utils.StringUtils;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GivePet extends JavaPlugin {

    private static GivePet instance;
    private static PetManager petManager;
    private static SQLTable requestsTable;
    private static SQLTable logsTable;

    @Override
    public void onEnable() {
        instance = this;
        petManager = new PetManager();

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

    public PetManager getPetManager() {
        return petManager;
    }


}
