package ru.spliterash.warehouse.view.windows.cargo;

import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import ru.spliterash.warehouse.database.BaseConstructor;
import ru.spliterash.warehouse.datamodel.CargoModelData;
import ru.spliterash.warehouse.datamodel.ClientDataModel;
import ru.spliterash.warehouse.datamodel.WarehouseDataModel;
import ru.spliterash.warehouse.other.Utils;
import ru.spliterash.warehouse.view.util.enums.EditAction;

public class CargoEditWindow extends VBox {
    @Getter
    private final CargoModelData model;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private ComboBox<WarehouseDataModel> warehouseBox;

    @FXML
    private ComboBox<ClientDataModel> clientBox;

    @FXML
    private TextField areaField;

    @FXML
    private TextField weightField;
    @Getter
    private EditAction action = EditAction.NONE;


    public CargoEditWindow(CargoModelData editModel) {
        Utils.loadFXML(this);
        if (editModel != null)
            this.model = editModel;
        else {
            this.model = new CargoModelData(-1, 0, 0, null, null);
            deleteButton.setDisable(true);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
        saveButton.setOnMouseClicked(e -> save());
        deleteButton.setOnMouseClicked(e -> delete());
        if (model.getId() == -1)
            deleteButton.setDisable(true);
        Utils.onlyNumber(areaField, weightField);
        //Если все TRUE то тогда это тоже TRUE, иначе FALSE
        BooleanBinding saveAvailable = Utils.andBinding(
                areaField.textProperty().isNotEmpty(),
                weightField.textProperty().isNotEmpty(),
                Utils.boxSelected(clientBox),
                Utils.boxSelected(warehouseBox)
        );
        //Инвертируем, так как это отрубает кнопку
        saveButton.disableProperty().bind(saveAvailable.not());

        warehouseBox.getItems().addAll(BaseConstructor.getBase().getWarehouses());
        clientBox.getItems().addAll(BaseConstructor.getBase().getClients());

        if (model.getWarehouse() != null) {
            warehouseBox
                    .getItems()
                    .stream()
                    .filter(w -> w.getId() == model.getWarehouse().getId())
                    .findFirst()
                    .ifPresent(w -> warehouseBox.getSelectionModel().select(w));
        }
        if (model.getClient() != null) {
            clientBox
                    .getItems()
                    .stream()
                    .filter(c -> c.getId() == model.getClient().getId())
                    .findFirst()
                    .ifPresent(c -> clientBox.getSelectionModel().select(c));
        }
        areaField.setText(String.valueOf(model.getArea()));
        weightField.setText(String.valueOf(model.getWeight()));
    }

    @FXML
    private void close() {
        Utils.closeWindow(this);
        action = EditAction.NONE;
    }

    @FXML
    private void delete() {
        model.delete();
        action = EditAction.DELETE;
        Utils.closeWindow(this);
    }

    @FXML
    private void save() {
        model.setArea(Integer.parseInt(areaField.getText()));
        model.setClient(clientBox.getValue());
        model.setWarehouse(warehouseBox.getValue());
        model.setWeight(Integer.parseInt(weightField.getText()));

        int leftSize = model.getWarehouse().getArea() - model.getWarehouse().getOccupied();
        int leftAfter = leftSize - model.getArea();
        if (leftAfter < 0) {
            Utils.showWaitAlert(Alert.AlertType.ERROR, "Невозможно сохранить", "На складе недостаточно места для этого груза", "На складе свободно только " + leftSize + " м.");
            return;
        }
        if (model.getId() == -1)
            action = EditAction.CREATE;
        else
            action = EditAction.UPDATE;
        model.save();
        Utils.closeWindow(this);
    }

    public void setWarehouse(int id) {
        warehouseBox
                .getItems()
                .stream()
                .filter(i -> i.getId() == id)
                .findFirst()
                .ifPresent(m -> warehouseBox.getSelectionModel().select(m));
    }

    public void setClient(int id) {
        clientBox
                .getItems()
                .stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .ifPresent(c -> clientBox.getSelectionModel().select(c));
    }
}
