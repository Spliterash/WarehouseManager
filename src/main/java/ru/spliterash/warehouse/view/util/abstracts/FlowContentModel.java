package ru.spliterash.warehouse.view.util.abstracts;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.Getter;
import ru.spliterash.warehouse.view.util.objects.FilteredListWrapper;

import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class FlowContentModel extends ContentModel {
    private final ObservableList<Node> source = FXCollections.observableArrayList();
    @Getter
    private final FilteredListWrapper wrapper;
    @Getter(AccessLevel.PROTECTED)
    private final StringProperty searchText;

    public FlowContentModel() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        FlowPane pane = new FlowPane();
        scroll.setContent(pane);
        pane.setStyle("-fx-padding: 35px 0 35px 35px;");
        pane.setHgap(35);
        pane.setVgap(35);
        wrapper = new FilteredListWrapper(pane, source, getFilter());

        VBox box = new VBox();
        TextField search = new TextField();
        searchText = search.textProperty();
        search.setPromptText("Поиск");
        search.textProperty().addListener(this::changed);
        VBox.setMargin(search, new Insets(15));
        box.getChildren().add(search);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        box.getChildren().add(scroll);
        setContent(box);
    }

    private void changed(ObservableValue<?> observable, String oldValue, String newValue) {
        wrapper.refresh();
    }


    protected abstract Node[] getFlowNodes();

    protected abstract Supplier<Predicate<Node>> getFilter();

    @Override
    public void refresh() {
        source.clear();
        for (Node node : getFlowNodes()) {
            if (node != null)
                source.add(node);
        }
        source.add(getAddNode());
        wrapper.refresh();
    }

    protected abstract Node getAddNode();
}
