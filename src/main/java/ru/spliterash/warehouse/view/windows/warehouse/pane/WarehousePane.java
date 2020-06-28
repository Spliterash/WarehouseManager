package ru.spliterash.warehouse.view.windows.warehouse.pane;

import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import ru.spliterash.warehouse.datamodel.WarehouseDataModel;
import ru.spliterash.warehouse.other.Utils;

public class WarehousePane extends PieChart {
    public WarehousePane(WarehouseDataModel model) {
        Utils.wrapTitle(this);
        setTitle(model.getAddress());
        int area = model.getArea();
        int occupied = model.getOccupied();
        int free = area - occupied;
        setLegendVisible(true);
        setTitleSide(Side.TOP);
        setLabelsVisible(false);
        getData().add(new PieChart.Data("Свободно " + free, free));
        getData().add(new PieChart.Data("Занято " + occupied, occupied));
        for (PieChart.Data data : getData()) {
            Tooltip tooltip = new Tooltip();
            Utils.tooltipTime(tooltip);
            tooltip.setText(data.getName() + " м.");
            Tooltip.install(data.getNode(), tooltip);
        }

    }


}
