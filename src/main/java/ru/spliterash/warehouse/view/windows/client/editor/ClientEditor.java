package ru.spliterash.warehouse.view.windows.client.editor;

import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Getter;
import ru.spliterash.warehouse.database.BaseConstructor;
import ru.spliterash.warehouse.datamodel.CargoModelData;
import ru.spliterash.warehouse.datamodel.ClientDataModel;
import ru.spliterash.warehouse.datamodel.ContactDataModel;
import ru.spliterash.warehouse.datamodel.ContactTypeDataModel;
import ru.spliterash.warehouse.other.Utils;
import ru.spliterash.warehouse.view.util.enums.EditAction;
import ru.spliterash.warehouse.view.windows.cargo.CargoEditWindow;
import ru.spliterash.warehouse.view.windows.warehouse.editor.WarehouseEditor;

import java.io.File;

public class ClientEditor extends TabPane {
    @Getter
    private final ClientDataModel client;
    @FXML
    private VBox cargoBox;
    @FXML
    private VBox contactBox;
    @FXML
    private ListView<ContactDataModel> contactView;
    @FXML
    private Label idLabel;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField middleNameField;
    @FXML
    private ChoiceBox<ContactTypeDataModel> contactTypeBox;
    @FXML
    private TextField contactField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;
    @FXML
    private ListView<CargoModelData> cargoList;
    @FXML
    private Label cargoId;
    @FXML
    private Label cargoWarehouse;
    @FXML
    private Label cargoWeight;
    @FXML
    private Label cargoArea;

    public ClientEditor(ClientDataModel client) {
        Utils.loadFXML(this);
        if (client == null) {
            this.client = new ClientDataModel(-1, null, null, null);
            client = this.client;
        } else
            this.client = client;
        contactView.getSelectionModel().selectedItemProperty().addListener(this::selectAnotherContact);
        contactView.setItems(FXCollections.observableList(this.client.getContact(), param -> new Observable[]{param.getValue(), param.getType()}));
        idLabel.setText(client.getId() == -1 ? "Требуется сохранение" : String.valueOf(client.getId()));
        firstNameField.setText(client.getFirstName());
        lastNameField.setText(client.getLastName());
        middleNameField.setText(client.getMiddleName());
        contactTypeBox.getItems().addAll(BaseConstructor.getBase().getContactTypes());

        getSelectionModel().selectedItemProperty().addListener(this::tabChange);
        {
            BooleanBinding f = firstNameField.textProperty().isNotEmpty();
            BooleanBinding l = lastNameField.textProperty().isNotEmpty();
            BooleanBinding m = middleNameField.textProperty().isNotEmpty();
            saveButton.disableProperty().bind(Utils.andBinding(
                    f,
                    l,
                    m
            ).not());
        }
        refreshCargoList();
        cargoList.getSelectionModel().selectedItemProperty().addListener(this::onChangeCargo);
        cargoList.setCellFactory(new Callback<ListView<CargoModelData>, ListCell<CargoModelData>>() {
            @Override
            public ListCell<CargoModelData> call(ListView<CargoModelData> param) {
                return new ListCell<CargoModelData>() {
                    @Override
                    protected void updateItem(CargoModelData item, boolean empty) {
                        if (item != null) {
                            setText(item.info(true, false, true, true));
                        } else
                            setText(null);
                        super.updateItem(item, empty);
                    }
                };
            }
        });
    }

    @FXML
    private void deleteClient() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "При удалении клиента, удалятся все его грузы", ButtonType.NO, ButtonType.YES);
        alert.setTitle("Удаление клиента");
        alert.setHeaderText("Подтвердите удаление клиента");
        if (alert.showAndWait().orElse(ButtonType.NO).equals(ButtonType.YES)) {
           getClient().delete();
           Utils.closeWindow(this);
        }
    }

    private void refreshCargoList() {
        cargoList.getItems().setAll(BaseConstructor.getBase().getClientCargo(client.getId()));
    }

    @FXML
    private void onChangeClick() {
        if (selectedCargo != null) {
            CargoEditWindow editor = new CargoEditWindow(selectedCargo);
            Utils.showAndWait(editor, "Редактирование груза", getScene().getWindow());
            switch (editor.getAction()) {
                case DELETE:
                    cargoList.getItems().remove(editor.getModel());
                    break;
                case CREATE:
                    cargoList.getItems().add(editor.getModel());
                    break;
                case UPDATE:
                    cargoList.refresh();
                    break;
            }
        }
    }

    @FXML
    private void onCargoAdd() {
        CargoEditWindow editor = new CargoEditWindow(null);
        editor.setClient(client.getId());
        Utils.showAndWait(editor, "Создание грузка", getScene().getWindow());
        if (editor.getAction().equals(EditAction.CREATE)) {
            cargoList.getItems().add(editor.getModel());
        }
    }

    private CargoModelData selectedCargo;

    public void onChangeCargo(ObservableValue<? extends CargoModelData> observable, CargoModelData oldValue, CargoModelData newValue) {
        selectedCargo = newValue;
        if (newValue != null) {
            cargoBox.setDisable(false);
            cargoId.setText(String.valueOf(newValue.getId()));
            cargoWarehouse.setText(newValue.getWarehouse().getAddress());
            cargoWarehouse.setOnMouseClicked(e -> {
                        WarehouseEditor editor = new WarehouseEditor(newValue.getWarehouse());
                        int id = newValue.getId();
                        Utils.showAndWait(editor,
                                "Редактирование склада",
                                getScene().getWindow());
                        refreshCargoList();
                        cargoList
                                .getItems()
                                .stream()
                                .filter(i -> i.getId() == id)
                                .findFirst()
                                .ifPresent(item -> cargoList.getSelectionModel().select(item));
                    }
            );
            cargoArea.setText(String.valueOf(newValue.getArea()));
            cargoWeight.setText(String.valueOf(newValue.getWeight()));
        } else {
            cargoBox.setDisable(true);
            cargoId.setText("");
            cargoWarehouse.setText("");
            cargoWarehouse.setOnMouseClicked(null);
            cargoWeight.setText("");
            cargoArea.setText("");
        }

    }

    private void bind(ContactDataModel model) {
        int id = model.getClientId();
        idLabel.setText(String.valueOf(id));
        Property<ContactTypeDataModel> type = model.getType();
        ContactTypeDataModel val = type.getValue();
        if (val != null)
            contactTypeBox.getSelectionModel().select(val);
        type.bind(contactTypeBox.getSelectionModel().selectedItemProperty());
        StringProperty v = model.getValue();
        contactField.textProperty().set(v.getValue());
        v.bind(contactField.textProperty());
    }

    private void unbind(ContactDataModel model) {
        model.getType().unbind();
        model.getValue().unbind();
    }

    private void selectAnotherContact(
            ObservableValue<? extends ContactDataModel> observable,
            ContactDataModel oldValue,
            ContactDataModel newValue) {
        if (oldValue != null)
            unbind(oldValue);
        if (newValue != null) {
            bind(newValue);
            contactBox.setDisable(false);
        } else
            contactBox.setDisable(true);


    }

    @FXML
    private void saveClick() {
        updateClient();

        client.save();
        client.saveContact(contactView.getItems());
        idLabel.setText(String.valueOf(client.getId()));
        Stage stage = (Stage) getScene().getWindow();
        stage.setTitle("Редактирование клиента");
        Utils.showWaitAlert(
                Alert.AlertType.INFORMATION,
                "Успешно",
                "Клиент сохранён",
                "Внесённые изменения сохранены");
    }

    @FXML
    private void contactAddButtonClick() {
        ContactDataModel model = new ContactDataModel(getClient().getId(), null, "");
        contactView.getItems().add(0, model);
        contactView.getSelectionModel().select(0);
    }

    @FXML
    private void deleteSelectedContact() {
        ContactDataModel selected = contactView.getSelectionModel().getSelectedItem();
        contactView.getItems().remove(selected);
    }

    @FXML
    private Tab exportTab;

    private void tabChange(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (newValue.equals(exportTab)) {
            getSelectionModel().select(oldValue);
            updateClient();
            if (client.isValid()) {
                FileChooser chooser = new FileChooser();
                chooser.setInitialFileName("Экспорт " + client.toString() + ".xlsx");
                chooser.setTitle("Выберите место экспорта информации о клиенте");
                File f = chooser.showSaveDialog(getScene().getWindow());
                if (f != null)
                    client.exportToExcel(f);
            } else {
                Utils.showWaitAlert(
                        Alert.AlertType.WARNING,
                        "Новый клиент",
                        "Экспорт невозможен",
                        "Экспорт невозможен, поскольку клиент только что создан");
            }

        }
    }

    private void updateClient() {
        client.setFirstName(firstNameField.getText());
        client.setLastName(lastNameField.getText());
        client.setMiddleName(middleNameField.getText());
    }
}
