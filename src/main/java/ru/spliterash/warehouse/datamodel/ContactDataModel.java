package ru.spliterash.warehouse.datamodel;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;
import ru.spliterash.warehouse.database.BaseConstructor;

import java.util.List;

@Getter
@Setter
public class ContactDataModel {
    private final int clientId;

    public ContactDataModel(int clientId, ContactTypeDataModel type, String value) {
        this.clientId = clientId;
        this.type = new SimpleObjectProperty<>(type);
        this.value = new SimpleStringProperty(value);
    }

    private Property<ContactTypeDataModel> type;
    private StringProperty value;

    public static List<ContactTypeDataModel> getTypes() {
        return BaseConstructor.getBase().getContactTypes();
    }

    @Override
    public String toString() {
        ContactTypeDataModel typeVal = type.getValue();
        String str;
        if (typeVal != null)
            str = typeVal.getName();
        else
            str = "Не определено";
        return str + "\n" + value.get();
    }
}
