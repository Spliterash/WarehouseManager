package ru.spliterash.warehouse.datamodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.spliterash.warehouse.database.BaseConstructor;
import ru.spliterash.warehouse.other.Utils;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
public class WarehouseDataModel {
    private int id;
    private String address;
    private int area;

    public int getOccupied() {
        return BaseConstructor.getBase().getWarehouseOccupied(id);
    }

    public List<CargoModelData> getCargo() {
        return BaseConstructor.getBase().getWarehouseCargo(id);
    }

    public void delete() {
        BaseConstructor.getBase().deleteWarehouse(id);
        id = -1;
    }

    public void save() {
        BaseConstructor.getBase().saveWarehouse(this);
    }

    @Override
    public String toString() {
        return address;
    }

    public void exportToExcel(File file) {
        List<CargoModelData> cargoList = getCargo();
        Workbook workbook = new XSSFWorkbook();
        String[][] infoSheet = new String[][]{
                {"ID склада", String.valueOf(getId())},
                {"Улица", getAddress()},
                {"Площадь", String.valueOf(getArea())}
        };
        String name = "Информация о складе";
        Utils.fillSheet(workbook, name, infoSheet);
        String[][] array = Stream.concat(
                Stream.of(new String[][]{{"ID груза", "Площадь", "Вес", "Владелец"}}),
                cargoList
                        .stream()
                        .map(c -> new String[]{
                                String.valueOf(c.getId()),
                                String.valueOf(c.getArea()),
                                String.valueOf(c.getWeight()),
                                c.getClient().toString()
                        })
        ).toArray(String[][]::new);
        Utils.fillSheet(workbook, "Грузы", array);
        Sheet sheet = workbook.getSheet(name);
        CellStyle style = Utils.getStyle(workbook);
        {
            Row newRow = sheet.createRow(sheet.getLastRowNum() + 1);
            {
                Cell cell = newRow.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellStyle(style);
                cell.setCellValue("Занято");
            }
            String cellAddress;
            {
                Cell cell = newRow.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellStyle(style);
                cell.setCellFormula("SUM(Грузы!B:B)");
                cellAddress = cell.getAddress().formatAsString();
            }
            Row nextRow = sheet.createRow(newRow.getRowNum() + 1);
            {
                Cell cell = nextRow.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellStyle(style);
                cell.setCellValue("Свободно");
            }
            {
                Cell cell = nextRow.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellStyle(style);
                cell.setCellFormula("B3-" + cellAddress);
            }
        }
        Utils.saveWorkbook(workbook, file);
    }
}
