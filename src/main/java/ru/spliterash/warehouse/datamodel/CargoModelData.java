package ru.spliterash.warehouse.datamodel;

import lombok.*;
import ru.spliterash.warehouse.database.BaseConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class CargoModelData {
    private int id;
    private int weight;
    private int area;
    private WarehouseDataModel warehouse;
    private ClientDataModel client;

    public void delete() {
        BaseConstructor.getBase().deleteCargo(id);
        id = -1;
    }

    public void save() {
        BaseConstructor.getBase().saveCargo(this);
    }

    public String info(boolean warehouse, boolean client, boolean area, boolean weight) {
        String[] info = new String[4];
        if (warehouse)
            info[0] = "Склад: " + this.warehouse.getAddress();
        if (client)
            info[1] = "Клиент: " + this.client.toString();
        if (area)
            info[2] = "Площадь: " + this.area;
        if (weight)
            info[3] = "Вес: " + this.weight;
        return Arrays.stream(info).filter(Objects::nonNull).collect(Collectors.joining("\n"));
    }
}
