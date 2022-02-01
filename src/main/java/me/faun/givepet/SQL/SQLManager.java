package me.faun.givepet.SQL;

import mc.obliviate.bloksqliteapi.SQLHandler;
import mc.obliviate.bloksqliteapi.sqlutils.DataType;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.faun.givepet.GivePet;

public class SQLManager extends SQLHandler {
    public SQLManager(GivePet plugin) {
        super(plugin.getDataFolder().getAbsolutePath());
        connect();
    }

    public void connect() {
        super.connect("database");
    }

    @Override
    public void onConnect() {
        System.out.println("SQLManager connected successfully");
    }

    public SQLTable createLogsTable() {
        final SQLTable sqlTable = new SQLTable("logs", "id")
                .addField("id", DataType.INTEGER, true, true, true)
                .addField("sender", DataType.TEXT)
                .addField("receiver", DataType.TEXT)
                .addField("time", DataType.TEXT)
                .addField("finished", DataType.TEXT);
        return sqlTable.create();
    }

    public SQLTable createRequestsTable() {
        final SQLTable sqlTable = new SQLTable("requests", "sender")
                .addField("sender", DataType.TEXT, true, true, true)
                .addField("receiver", DataType.TEXT, true, false, false)
                .addField("time", DataType.TEXT, true, false, false)
                .addField("finished", DataType.TEXT, true, false, false);
        return sqlTable.create();
    }

    public void createRow(SQLTable sqlTable, String[] values, String id) {
        sqlTable.insert(sqlTable.createUpdate(id)
                .putData("sender", values[0])
                .putData("receiver", values[1])
                .putData("time", values[2])
                .putData("finished", values[3]));
    }

    public void logRequest(String sender, String receiver, String time, String finished) {
        SQLTable logsTable = GivePet.getInstance().getLogsTable();
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