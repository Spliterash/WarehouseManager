package ru.spliterash.warehouse.view.util.animation.elements;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import ru.spliterash.warehouse.other.Utils;
import ru.spliterash.warehouse.view.util.enums.ImageManager;

public class Hover extends AnchorPane {
    @FXML
    private ImageView view;
    @FXML
    private Label action;

    public Hover(Image image, String action) {
        Utils.loadFXML(this);
        view.setImage(image);
        this.action.setText(action);
    }


    public Hover(ImageManager image, String action) {
        this(image.getResizedFx(200, -1), action);
    }
}
