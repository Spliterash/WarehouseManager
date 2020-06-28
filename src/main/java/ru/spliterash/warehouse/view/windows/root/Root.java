package ru.spliterash.warehouse.view.windows.root;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import ru.spliterash.warehouse.other.Utils;
import ru.spliterash.warehouse.view.util.abstracts.ContentModel;
import ru.spliterash.warehouse.view.windows.client.ClientWindow;
import ru.spliterash.warehouse.view.windows.warehouse.WarehouseWindow;

import java.util.HashMap;
import java.util.Map;

public class Root extends TabPane {
    private final Map<Tab, ContentModel> tabContents = new HashMap<>();

    public Root() {
        Utils.loadFXML(this);
        tabContents.put(warehouseTab, new WarehouseWindow());
        tabContents.put(clientTab, new ClientWindow());
        for (Map.Entry<Tab, ContentModel> entry : tabContents.entrySet()) {
            entry.getKey().contentProperty().bind(entry.getValue().getContent());
            entry.getValue().refresh();
        }
        getSelectionModel().selectedItemProperty().addListener(this::onTabClick);
    }

    private void onTabClick(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        ContentModel content = tabContents.get(newValue);
        if (content != null) {
            content.refresh();
        }
    }


    @FXML
    private Tab warehouseTab;

    @FXML
    private Tab clientTab;


}
