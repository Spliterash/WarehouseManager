package ru.spliterash.warehouse.datamodel;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.spliterash.warehouse.database.BaseConstructor;
import ru.spliterash.warehouse.other.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class ClientDataModel {
    private int id;
    private String firstName;
    private String lastName;
    private String middleName;

    @Override
    public String toString() {
        return lastName + " " + firstName + " " + middleName;
    }

    public void exportToExcel(File destination) {
        Workbook workbook = new XSSFWorkbook();
        {
            //Экспорт грузов клиента
            {
                List<CargoModelData> list = BaseConstructor.getBase().getClientCargo(id);
                //Преобразуем всё в таблицу | ID | Склад | Площадь | Вес
                Utils.fillSheet(workbook, "Грузы",
                        Stream.concat(
                                Stream.of(new String[][]{new String[]{"ID", " Склад", "Площадь", "Вес"}}),
                                list
                                        .stream()
                                        .map(c -> new String[]{
                                                String.valueOf(c.getId()),
                                                c.getWarehouse().getAddress(),
                                                String.valueOf(c.getArea()),
                                                String.valueOf(c.getWeight())
                                        }))
                                .toArray(String[][]::new));
            }
            //Экспорт контактов
            {
                List<ContactDataModel> list = getContact();
                Utils.fillSheet(workbook, "Контакты",
                        Stream.concat(
                                Stream.of(new String[][]{new String[]{"Тип контакта", "Значение"}}),
                                list
                                        .stream()
                                        .map(c -> new String[]{
                                                String.valueOf(c.getType().getValue().getName()),
                                                String.valueOf(c.getValue().getValue())
                                        })).toArray(String[][]::new));
            }
            Utils.saveWorkbook(workbook, destination);
        }
    }

    public List<ContactDataModel> getContact() {
        if (id == -1)
            return new ArrayList<>(0);
        else
            return BaseConstructor.getBase().getClientContact(id)
                    .values()
                    .stream()
                    .findFirst()
                    .orElseGet(() -> new ArrayList<>(0));
    }

    public String info() {
        return toString() + "\n"
                + getContact()
                .stream()
                .limit(2)
                .map(c -> c.getValue().get())
                .collect(Collectors.joining("\n"));
    }

    public boolean isValid() {
        return firstName != null && lastName != null && middleName != null && id != -1;
    }

    public void saveContact(List<ContactDataModel> contacts) {
        BaseConstructor.getBase().saveClientContact(id, contacts);
    }

    public void save() {
        BaseConstructor.getBase().saveClient(this);
    }

    public void delete() {
        BaseConstructor.getBase().deleteClient(id);
        id = -1;
    }
}
