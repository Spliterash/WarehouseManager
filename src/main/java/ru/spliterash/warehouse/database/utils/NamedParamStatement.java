package ru.spliterash.warehouse.database.utils;

import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParamStatement {
    private static final Pattern findParametersPattern = Pattern.compile("(?<!')(:[\\w]*)(?!')");
    private final String query;

    public NamedParamStatement(@Language("SQL") String query) {
        this.query = query;
    }

    public int executeUpdate(Connection connection, Map<String, Object> map) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(query.replaceAll(findParametersPattern.pattern(), "?"));
        fillStatement(prepared, map);
        return prepared.executeUpdate();
    }

    public int executeQuery(Connection connection, Map<String, Object> map) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(query.replaceAll(findParametersPattern.pattern(), "?"));
        fillStatement(prepared, map);
        return prepared.executeUpdate();
    }

    private void fillStatement(PreparedStatement statement, Map<String, Object> map) throws SQLException {
        Matcher matcher = findParametersPattern.matcher(query);
        for (int i = 0; matcher.find(); i++) {
            String key = matcher.group().substring(1);
            Object obj = map.get(key);
            if (obj != null) {
                setValue(statement, i, obj);
            }
        }
    }

    private void setValue(PreparedStatement statement, int i, Object obj) throws SQLException {
        int index = i + 1;
        if (obj instanceof String)
            statement.setString(index, (String) obj);
        else if (obj instanceof Double)
            statement.setDouble(index, (double) obj);
        else if (obj instanceof Integer)
            statement.setInt(index, (Integer) obj);
        else if (obj instanceof Date)
            statement.setDate(index, (Date) obj);
        else
            statement.setObject(index, obj);

    }
}