package ru.spliterash.warehouse.datamodel;

import lombok.Value;

@Value
public class ContactTypeDataModel {
    int id;
    String name;

    @Override
    public String toString() {
        return name;
    }
}
