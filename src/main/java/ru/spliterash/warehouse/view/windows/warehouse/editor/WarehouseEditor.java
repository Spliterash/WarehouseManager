package ru.spliterash.warehouse.view.windows.warehouse.editor;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import ru.spliterash.warehouse.datamodel.CargoModelData;
import ru.spliterash.warehouse.datamodel.ClientDataModel;
import ru.spliterash.warehouse.datamodel.WarehouseDataModel;
import ru.spliterash.warehouse.other.Utils;
import ru.spliterash.warehouse.view.util.enums.EditAction;
import ru.spliterash.warehouse.view.windows.cargo.CargoEditWindow;
import ru.spliterash.warehouse.view.windows.client.editor.ClientEditor;
import ru.spliterash.warehouse.view.windows.warehouse.name.WarehouseNameEditor;

import java.io.File;
import java.util.*;

public class WarehouseEditor extends TabPane {
    private final WarehouseDataModel model;
    @FXML
    private Tab exportTab;
    @FXML
    private Tab settingTab;
    @FXML
    private Label weightLabel;
    @FXML
    private PieChart clientsGraphic;

    @FXML
    private ListView<CargoModelData> cargoList;

    @FXML
    private Label idLabel;

    @FXML
    private Label clientLabel;

    @FXML
    private Label areaLabel;

    @FXML
    private Button changeCargo;
    private CargoModelData selected;

    public WarehouseEditor(WarehouseDataModel model) {
        Utils.loadFXML(this);
        this.model = model;
        //Добавление слушателя на листвайф
        cargoList.getSelectionModel().selectedItemProperty().addListener(this::onCargoSelect);
        refreshCargo();
        changeCargo.setOnAction(this::onEditButtonClick);
        {
            WarehouseNameEditor editor = new WarehouseNameEditor(model);
            editor.setOnSave(() -> Utils.showWaitAlert(
                    Alert.AlertType.INFORMATION,
                    "Операция выполнена",
                    "Склад успешно сохранён",
                    ""
            ));
            settingTab.setContent(editor);
        }
        cargoList.setCellFactory(new Callback<ListView<CargoModelData>, ListCell<CargoModelData>>() {
            @Override
            public ListCell<CargoModelData> call(ListView<CargoModelData> param) {
                return new ListCell<CargoModelData>() {
                    @Override
                    protected void updateItem(CargoModelData item, boolean empty) {
                        if (item != null)
                            setText(item.info(false, true, true, true));
                        else
                            setText(null);
                        super.updateItem(item, empty);
                    }
                };
            }
        });
        getSelectionModel().selectedItemProperty().addListener(this::onTabChange);
    }

    private void onTabChange(ObservableValue<?> observable, Tab oldTab, Tab newTab) {
        if (newTab.equals(exportTab)) {
            getSelectionModel().select(oldTab);
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("Экспорт " + model.toString() + ".xlsx");
            chooser.setTitle("Выберите место экспорта информации о складе");
            File f = chooser.showSaveDialog(getScene().getWindow());
            if (f != null)
                model.exportToExcel(f);
        }
    }


    @FXML
    private void addCargo() {
        if (model.getId() == -1) {
            Utils.showWaitAlert(
                    Alert.AlertType.ERROR,
                    "Ошибка",
                    "Этот склад не сохранён",
                    "Перед добавлением груза необходимо сохранить склад");
        } else {
            CargoEditWindow edit = new CargoEditWindow(null);
            edit.setWarehouse(model.getId());
            Utils.showAndWait(edit, "Создание груза", getScene().getWindow());
            if (!edit.getAction().equals(EditAction.NONE)) {
                refreshCargo();
            }

        }
    }

    private void onEditButtonClick(ActionEvent actionEvent) {
        CargoEditWindow edit = new CargoEditWindow(selected);
        Utils.showAndWait(edit, "Редактирование груза", getScene().getWindow());
        switch (edit.getAction()) {
            case CREATE:
                cargoList.getItems().add(edit.getModel());
                break;
            case DELETE:
                cargoList.getItems().remove(edit.getModel());
                break;
            case UPDATE:
                cargoList.refresh();
                break;
        }

    }

    private void openClient(ClientDataModel model) {
        ClientEditor editor = new ClientEditor(model);
        Utils.showAndWait(editor, "Редактирование клиента", getScene().getWindow());
        refreshCargo();
    }

    private void refreshCargo() {
        List<CargoModelData> list = model.getCargo();
        cargoList.getItems().setAll(list);
        Map<ClientDataModel, Set<CargoModelData>> grouped = new LinkedHashMap<>();
        for (CargoModelData data : list) {
            Set<CargoModelData> userSet = grouped.computeIfAbsent(data.getClient(), clientDataModel -> new HashSet<>());
            userSet.add(data);
        }

        ObservableList<PieChart.Data> data = clientsGraphic.getData();
        data.clear();
        int sum = 0;
        for (Map.Entry<ClientDataModel, Set<CargoModelData>> entry : grouped.entrySet()) {
            ClientDataModel client = entry.getKey();
            Set<CargoModelData> set = entry.getValue();
            int areaSum = set
                    .stream()
                    .mapToInt(CargoModelData::getArea)
                    .sum();
            sum += areaSum;
            PieChart.Data dataElement = new PieChart.Data(client.toString() + " (" + areaSum + " м.)", areaSum);
            data.add(dataElement);
            Node node = dataElement.getNode();
            Tooltip tip = new Tooltip(dataElement.getName());
            Utils.tooltipTime(tip);
            Tooltip.install(node, tip);
            node.setOnMouseClicked(e -> openClient(client));
        }
        int free = model.getArea() - sum;
        PieChart.Data freeChart = new PieChart.Data("Свободно (" + free + " м.)", free);
        data.add(freeChart);
        Tooltip tip = new Tooltip(free + " м.");
        Utils.tooltipTime(tip);
        Tooltip.install(freeChart.getNode(), tip);

    }

    private void select(CargoModelData data) {
        selected = data;
        if (data != null) {
            idLabel.setText(String.valueOf(data.getId()));
            areaLabel.setText(String.valueOf(data.getArea()));
            clientLabel.setText(String.valueOf(data.getClient().toString()));
            clientLabel.setOnMouseClicked(e -> {
                int id = data.getId();
                openClient(data.getClient());
                cargoList
                        .getItems()
                        .stream()
                        .filter(i -> i.getId() == id)
                        .findFirst()
                        .ifPresent(m -> cargoList.getSelectionModel().select(m));
            });
            weightLabel.setText(String.valueOf(data.getWeight()));
        } else {
            idLabel.setText("");
            areaLabel.setText("");
            clientLabel.setText("");
            clientLabel.setOnMouseClicked(null);
            weightLabel.setText("");
        }
    }

    private void onCargoSelect(ObservableValue<? extends CargoModelData> observable, CargoModelData oldValue, CargoModelData newValue) {
        changeCargo.setDisable(newValue == null);
        select(newValue);
    }
}
