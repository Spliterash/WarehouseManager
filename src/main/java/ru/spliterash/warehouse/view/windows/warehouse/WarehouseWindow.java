package ru.spliterash.warehouse.view.windows.warehouse;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import ru.spliterash.warehouse.database.BaseConstructor;
import ru.spliterash.warehouse.datamodel.WarehouseDataModel;
import ru.spliterash.warehouse.other.Utils;
import ru.spliterash.warehouse.view.util.abstracts.FlowContentModel;
import ru.spliterash.warehouse.view.util.animation.AnimationPane;
import ru.spliterash.warehouse.view.util.animation.elements.Hover;
import ru.spliterash.warehouse.view.util.enums.ImageManager;
import ru.spliterash.warehouse.view.windows.warehouse.editor.WarehouseEditor;
import ru.spliterash.warehouse.view.windows.warehouse.name.WarehouseNameEditor;
import ru.spliterash.warehouse.view.windows.warehouse.pane.WarehousePane;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class WarehouseWindow extends FlowContentModel {

    public WarehouseWindow() {

    }

    @Override
    protected Node[] getFlowNodes() {
        List<WarehouseDataModel> list = BaseConstructor.getBase().getWarehouses();
        Node[] nodes = new Node[list.size()];
        for (int i = 0; i < list.size(); i++) {
            WarehouseDataModel current = list.get(i);
            nodes[i] = new WarehouseNode(current);
        }
        return nodes;
    }

    @Override
    protected Supplier<Predicate<Node>> getFilter() {
        return () -> (Predicate<Node>) node -> {
            if (node instanceof WarehouseNode) {
                WarehouseNode pane = (WarehouseNode) node;
                String text = getSearchText().get();
                if (text.isEmpty())
                    return true;
                else
                    return pane.model.toString().toLowerCase().contains(text.toLowerCase());
            } else {
                return true;
            }
        };
    }

    @Override
    protected Node getAddNode() {
        return new AnimationPane() {
            {
                super.update();
            }

            @Override
            protected void onClick(MouseEvent mouseEvent) {
                WarehouseNameEditor editor = new WarehouseNameEditor(null);
                editor.setOnSave(() -> Utils.closeWindow(editor));
                Utils.showAndWait(editor, "Добавление склада", getContent().get().getScene().getWindow());
                if (editor.isSaved()) {
                    openWarehouse(editor.getModel());
                }
            }

            @Override
            protected Node getDefault() {
                return new Hover(ImageManager.PLUS, "Добавить склад");
            }
        };
    }

    private void openWarehouse(WarehouseDataModel model) {
        WarehouseEditor editor = new WarehouseEditor(model);
        Utils.showAndWait(
                editor,
                "Редактирование склада",
                getContent().get().getScene().getWindow());
        refresh();
    }

    private class WarehouseNode extends AnimationPane {
        private final WarehouseDataModel model;

        public WarehouseNode(WarehouseDataModel model) {
            this.model = model;
            super.update();
        }

        @Override
        protected void onClick(MouseEvent mouseEvent) {
            openWarehouse(model);
        }

        @Override
        protected Node getDefault() {
            return new WarehousePane(model);
        }
    }
}
