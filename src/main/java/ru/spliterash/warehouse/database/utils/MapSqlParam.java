package ru.spliterash.warehouse.database.utils;

import java.util.HashMap;

public class MapSqlParam extends HashMap<String, Object> {
    public MapSqlParam(String key, Object value) {
        put(key, value);
    }

    public MapSqlParam() {

    }

    public MapSqlParam add(String key, Object value) {
        put(key, value);
        return this;
    }
}
