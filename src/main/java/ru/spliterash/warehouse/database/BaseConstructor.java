package ru.spliterash.warehouse.database;


import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.SQLException;

public class BaseConstructor {
    private static final BaseConstructor baseConstructor = new BaseConstructor();
    Base base;

    private BaseConstructor() {
        try {
            reloadBase();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка перезагрузки базы");
            alert.setHeaderText("Программа будет закрыта, так как не удалось создать базу");
            alert.setContentText(ex.getLocalizedMessage());
            alert.setOnCloseRequest(e -> Platform.exit());
            alert.showAndWait();
        }
    }

    public static Base getBase() {
        return baseConstructor.getDataBase();
    }

    public Base getDataBase() {
        return base;
    }

    public void reloadBase() throws SQLException {
        loadSQLite();

    }

    private void loadSQLite() throws SQLException {
        base = new SQLite("base");
    }

}
