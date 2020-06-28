package ru.spliterash.warehouse.database;

import org.intellij.lang.annotations.Language;
import org.sqlite.JDBC;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class SQLite extends Base {
    private final File dataFolder;
    @Language("SQLite")
    private static final String creationString =
            "-- Creator:       MySQL Workbench 8.0.19/ExportSQLite Plugin 0.1.0\n" +
                    "-- Author:        admin\n" +
                    "-- Caption:       New Model\n" +
                    "-- Project:       Name of the project\n" +
                    "-- Changed:       2020-06-18 16:41\n" +
                    "-- Creator:       MySQL Workbench 8.0.19/ExportSQLite Plugin 0.1.0\n" +
                    "-- Author:        admin\n" +
                    "-- Caption:       New Model\n" +
                    "-- Project:       Name of the project\n" +
                    "-- Changed:       2020-06-18 16:41\n" +
                    "-- Created:       2020-06-10 21:25\n" +
                    "PRAGMA foreign_keys = OFF;\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS \"warehouse\"\n" +
                    "(\n" +
                    "    \"id\"      INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                    "    \"address\" VARCHAR(256)                      NOT NULL,\n" +
                    "    \"area\"    INTEGER                           NOT NULL\n" +
                    ");\n" +
                    "CREATE TABLE IF NOT EXISTS \"contact_type\"\n" +
                    "(\n" +
                    "    \"id\"   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                    "    \"name\" VARCHAR(45)                       NOT NULL,\n" +
                    "    CONSTRAINT \"name_UNIQUE\"\n" +
                    "        UNIQUE (\"name\")\n" +
                    ");\n" +
                    "CREATE TABLE IF NOT EXISTS \"client\"\n" +
                    "(\n" +
                    "    \"id\"          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                    "    \"first_name\"  VARCHAR(256)                      NOT NULL,\n" +
                    "    \"last_name\"   VARCHAR(256)                      NOT NULL,\n" +
                    "    \"middle_name\" VARCHAR(256)                      NOT NULL\n" +
                    ");\n" +
                    "CREATE TABLE IF NOT EXISTS \"contact\"\n" +
                    "(\n" +
                    "    \"id\"              INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                    "    \"client_id\"       VARCHAR(45)                       NOT NULL,\n" +
                    "    \"contact_type_id\" INTEGER                           NOT NULL,\n" +
                    "    \"value\"           VARCHAR(256)                      NOT NULL,\n" +
                    "    CONSTRAINT \"fk_contact_client\"\n" +
                    "        FOREIGN KEY (\"client_id\")\n" +
                    "            REFERENCES \"client\" (\"id\")\n" +
                    "            ON DELETE CASCADE,\n" +
                    "    CONSTRAINT \"fk_contact_contact_type1\"\n" +
                    "        FOREIGN KEY (\"contact_type_id\")\n" +
                    "            REFERENCES \"contact_type\" (\"id\")\n" +
                    ");\n" +
                    "CREATE INDEX IF NOT EXISTS \"contact.fk_contact_client_idx\" ON \"contact\" (\"client_id\");\n" +
                    "CREATE INDEX IF NOT EXISTS \"contact.fk_contact_contact_type1_idx\" ON \"contact\" (\"contact_type_id\");\n" +
                    "CREATE TABLE IF NOT EXISTS \"cargo\"\n" +
                    "(\n" +
                    "    \"id\"           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                    "    \"weight\"       INTEGER                           NOT NULL,\n" +
                    "    \"area\"         INTEGER                           NOT NULL,\n" +
                    "    \"warehouse_id\" INTEGER                           NOT NULL,\n" +
                    "    \"client_id\"    INTEGER                           NOT NULL,\n" +
                    "    CONSTRAINT \"fk_cargo_warehouse1\"\n" +
                    "        FOREIGN KEY (\"warehouse_id\")\n" +
                    "            REFERENCES \"warehouse\" (\"id\")\n" +
                    "            ON DELETE CASCADE,\n" +
                    "    CONSTRAINT \"fk_cargo_client1\"\n" +
                    "        FOREIGN KEY (\"client_id\")\n" +
                    "            REFERENCES \"client\" (\"id\")\n" +
                    "            ON DELETE CASCADE\n" +
                    ");\n" +
                    "CREATE INDEX IF NOT EXISTS \"cargo.fk_cargo_warehouse1_idx\" ON \"cargo\" (\"warehouse_id\");\n" +
                    "CREATE INDEX IF NOT EXISTS \"cargo.fk_cargo_client1_idx\" ON \"cargo\" (\"client_id\");\n" +
                    "PRAGMA foreign_keys = ON;\n" +
                    "\n" +
                    "INSERT INTO contact_type (name)\n" +
                    "VALUES ('Телефон'),\n" +
                    "       ('Почта'),\n" +
                    "       ('Skype'),\n" +
                    "       ('Офис'),\n" +
                    "       ('Дискорд'),\n" +
                    "       ('Телеграм')\n" +
                    "ON CONFLICT DO NOTHING" +
                    ";";

    SQLite(String name) throws SQLException {
        dataFolder = new File(name + ".db");
        if (!dataFolder.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                dataFolder.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("File write error: " + name + ".db");
            }
        }
        try {
            Class<JDBC> z = JDBC.class;
        } catch (Exception ex) {
            throw new IllegalArgumentException("jdbc driver unavailable!");
        }
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            for (String s : creationString.split(";")) {
                if (!s.isEmpty())
                    statement.executeUpdate(s);
            }
        }
        load();
    }


    @Override
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
    }
}
