package ru.spliterash.warehouse.view.util.objects;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Для фильтрации {@link Node} в любых {@link Pane}
 */
@Getter
public class FilteredListWrapper {
    private final ObservableList<Node> children;
    private final FilteredList<Node> filtered;
    private final Supplier<Predicate<Node>> predicate;

    public FilteredListWrapper(Pane pane, ObservableList<Node> source, Supplier<Predicate<Node>> filter) {
        this.children = pane.getChildren();
        this.filtered = new FilteredList<>(source);
        this.predicate = filter;
        Bindings.bindContent(children,filtered);

    }

    public void refresh() {
        filtered.setPredicate(predicate.get());
    }
}
