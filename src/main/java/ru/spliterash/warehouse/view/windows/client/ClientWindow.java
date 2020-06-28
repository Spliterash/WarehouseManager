package ru.spliterash.warehouse.view.windows.client;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import ru.spliterash.warehouse.database.BaseConstructor;
import ru.spliterash.warehouse.datamodel.ClientDataModel;
import ru.spliterash.warehouse.datamodel.ContactDataModel;
import ru.spliterash.warehouse.other.Utils;
import ru.spliterash.warehouse.view.util.abstracts.FlowContentModel;
import ru.spliterash.warehouse.view.util.animation.AnimationPane;
import ru.spliterash.warehouse.view.util.animation.elements.Hover;
import ru.spliterash.warehouse.view.util.enums.ImageManager;
import ru.spliterash.warehouse.view.windows.client.editor.ClientEditor;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ClientWindow extends FlowContentModel {
    @Override
    protected Node[] getFlowNodes() {
        List<ClientDataModel> list = BaseConstructor.getBase().getClients();
        Node[] clients = new Node[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ClientDataModel model = list.get(i);
            clients[i] = new ClientPane(model);

        }
        return clients;
    }

    @Override
    protected Supplier<Predicate<Node>> getFilter() {
        return () -> (Predicate<Node>) node -> {
            if (node instanceof ClientPane) {
                ClientPane pane = (ClientPane) node;
                String text = getSearchText().get();
                if (text.isEmpty())
                    return true;
                else
                    return pane.model.toString().toLowerCase().contains(text.toLowerCase());
            }
            return true;
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
                ClientEditor newClient = new ClientEditor(null);
                Utils.showAndWait(newClient, "Создание клиента", getContent().get().getScene().getWindow());
                refresh();
            }

            @Override
            protected Node getDefault() {
                return new Hover(ImageManager.PLUS, "Добавить клиента");
            }
        };
    }

    private class ClientPane extends AnimationPane {
        private final ClientDataModel model;

        public ClientPane(ClientDataModel label) {
            this.model = label;
            super.update();
        }

        @Override
        protected void onClick(MouseEvent mouseEvent) {
            ClientEditor newClient = new ClientEditor(model);
            Utils.showAndWait(newClient, "Редактирование клиента", getContent().get().getScene().getWindow());
            refresh();
        }

        @Override
        protected Node getDefault() {
            VBox box = new VBox();
            box.setPadding(new Insets(5, 15, 15, 15));
            Label label = new Label(model.toString());
            label.setWrapText(true);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-size: 20px;");
            List<ContactDataModel> clientContact = model.getContact();
            box.getChildren().add(new StackPane(label));
            if (clientContact.size() > 0) {

                box.getChildren().add(new Separator(Orientation.HORIZONTAL));
                for (int i = 0; i < clientContact.size() && i < 2; i++) {
                    ContactDataModel contact = clientContact.get(i);
                    box.getChildren().add(new Label(contact.toString()));
                }
            }
            return box;
        }
    }
}
