package me.faun.givepet.utils;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUtils {
    /**
     * This will return the resultSet's value from a column.
     *
     * @param resultSet the resultSet that needs to be checked.
     * @param column the column to check.
     * @return the string value on the column.
     */
    public static String getStringFromResultSet(@NotNull ResultSet resultSet, String column) {
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
