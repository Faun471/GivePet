package me.faun.givepet.sql;

import mc.obliviate.bloksqliteapi.SQLHandler;
import mc.obliviate.bloksqliteapi.sqlutils.DataType;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.GivePet;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class SQLManager extends SQLHandler {
    private final GivePet plugin;

    public SQLManager(GivePet plugin) {
        super(plugin.getDataFolder().getAbsolutePath());
        super.connect("database");
        this.plugin = plugin;
    }

    @Override
    public void onConnect() {
        Bukkit.getLogger().log(Level.INFO, "SQLManager connected successfully");
    }

    public SQLTable createLogsTable() {
        final SQLTable sqlTable = new SQLTable("logs", "id")
                .addField("id", DataType.INTEGER, true, true, true)
                .addField("sender", DataType.TEXT)
                .addField("receiver", DataType.TEXT)
                .addField("time", DataType.INTEGER)
                .addField("finished", DataType.TEXT);
        return sqlTable.create();
    }

    public SQLTable createRequestsTable() {
        final SQLTable sqlTable = new SQLTable("requests", "sender")
                .addField("sender", DataType.TEXT, true, true, true)
                .addField("receiver", DataType.TEXT, true, false, false)
                .addField("time", DataType.INTEGER, true, false, false)
                .addField("finished", DataType.TEXT, true, false, false);
        return sqlTable.create();
    }

    public void createRow(SQLTable sqlTable, String id, String... values) {
        sqlTable.insert(sqlTable.createUpdate(id)
                .putData("sender", values[0])
                .putData("receiver", values[1])
                .putData("time", values[2])
                .putData("finished", values[3]));
    }

    public void logRequest(String sender, String receiver, long time, String finished) {
        SQLTable logsTable = plugin.getLogsTable();
        int id = logsTable.getSingleHighest("id") != null ? Integer.parseInt(logsTable.getSingleHighest("id")) + 1 : 1;
        logsTable.insert(logsTable.createUpdate("id")
                .putData("id", id)
                .putData("sender", sender)
                .putData("receiver", receiver)
                .putData("time", time)
                .putData("finished", finished)
        );
    }

    public void clearTable(SQLTable sqlTable) {
        SQLHandler.sqlUpdate("DELETE FROM " + sqlTable.getTableName() + ";" + "\nVACUUM;");
    }
}