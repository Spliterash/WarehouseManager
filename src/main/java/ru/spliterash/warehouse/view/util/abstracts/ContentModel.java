package ru.spliterash.warehouse.view.util.abstracts;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Parent;

public abstract class ContentModel {

    private final ReadOnlyObjectWrapper<Parent> contentWrapper;

    public ContentModel() {
        contentWrapper = new ReadOnlyObjectWrapper<>();
    }

    public ReadOnlyObjectProperty<Parent> getContent() {
        return contentWrapper.getReadOnlyProperty();
    }

    protected final void setContent(Parent parent) {
        contentWrapper.set(parent);
    }

    public abstract void refresh();
}
