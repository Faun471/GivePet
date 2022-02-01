package me.faun.givepet.Utils;

import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUtils {

    public static String hasRequest(SQLTable requestsTable, Player player) {
        return requestsTable.getString("sender", "receiver");
    }

    public static String getStringFromResultSet(ResultSet resultSet, String column) {
        try {
            if (resultSet.isClosed()) {
                return "null";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            return resultSet.getString(column);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "null";
    }
}
