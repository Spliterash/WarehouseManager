package ru.spliterash.warehouse.view.windows.warehouse.name;

import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import ru.spliterash.warehouse.datamodel.WarehouseDataModel;
import ru.spliterash.warehouse.other.Utils;

public class WarehouseNameEditor extends VBox {
    @Getter
    private final WarehouseDataModel model;
    @FXML
    private Button deleteButton;
    @FXML
    private Label idLabel;

    @FXML
    private TextField addressField;

    @FXML
    private TextField areaField;

    @FXML
    private Button saveButton;
    @Getter
    private boolean saved = false;
    @Setter
    private Runnable onSave;

    @FXML
    private void saveClick() {
        model.setArea(Integer.parseInt(areaField.getText()));
        model.setAddress(addressField.getText());
        model.save();
        saved = true;
        if (onSave != null) {
            onSave.run();
        }
    }

    @FXML
    private void onDeleteClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "При удалении склада, удалятся все его грузы", ButtonType.NO, ButtonType.YES);
        alert.setTitle("Удаление склада");
        alert.setHeaderText("Подтвердите удаление склада");
        if (alert.showAndWait().orElse(ButtonType.NO).equals(ButtonType.YES)) {
            getModel().delete();
            Utils.closeWindow(this);
        }
    }

    public WarehouseNameEditor(WarehouseDataModel model) {
        Utils.loadFXML(this);
        if (model != null) {
            addressField.setText(model.getAddress());
            idLabel.setText(model.getId() != -1 ? String.valueOf(model.getId()) : "Требуется сохранение");
            areaField.setText(String.valueOf(model.getArea()));
        } else {
            model = new WarehouseDataModel(-1, null, 0);
            deleteButton.setVisible(false);
            deleteButton.setDisable(true);
        }
        this.model = model;
        Utils.onlyNumber(areaField);
        BooleanBinding bind = Utils.andBinding(
                addressField.textProperty().isNotEmpty(),
                areaField.textProperty().isNotEmpty()
        );
        saveButton.disableProperty().bind(bind.not());
    }
}
