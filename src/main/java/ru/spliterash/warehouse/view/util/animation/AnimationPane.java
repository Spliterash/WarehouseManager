package ru.spliterash.warehouse.view.util.animation;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import ru.spliterash.warehouse.other.Utils;

@Getter(AccessLevel.PROTECTED)
public abstract class AnimationPane extends AnchorPane {

    private Node defaultNode;

    @FXML
    private final AnchorPane root;

    public AnimationPane() {
        setPrefHeight(200);
        setPrefWidth(200);
        root = new AnchorPane();
        root.getStyleClass().addAll("bg-secondary");
        Rectangle rectangle = new Rectangle(200, 200);
        rectangle.setArcHeight(15D);
        rectangle.setArcWidth(15D);
        Utils.setAnchorProperty(root);
        getChildren().add(root);
        root.setClip(rectangle);
        Duration animationTime = Duration.millis(350);
        //Подготавливам анимацию тени
        DropShadow shadow = new DropShadow();
        shadow.setBlurType(BlurType.TWO_PASS_BOX);
        shadow.setRadius(0);
        Interpolator interpolator = Interpolator.EASE_BOTH;

        //Объект который активирует и деактивирует все анимации по ховерПроперти
        //Запихиваем всё в таймлайн
        init();
        Timeline timeline = new Timeline();
        ObservableList<KeyFrame> frames = timeline.getKeyFrames();
        frames.add(
                new KeyFrame(Duration.ZERO,
                        e -> {
                            if (timeline.getRate() == 1) {
                                this.setEffect(shadow);
                            } else if (timeline.getRate() == -1) {
                                this.setEffect(null);
                            }
                        },

                        getStartFrame(shadow, interpolator)
                ));
        frames.add(
                new KeyFrame(animationTime,
                        getEndFrame(shadow, interpolator)
                ));

        //Запихиваем это в обёртку
        new CancelableAnimationWrapper<>(this.hoverProperty(), timeline);
        root.setOnMouseClicked(this::onClick);
    }

    protected KeyValue[] getEndFrame(DropShadow shadow, Interpolator interpolator) {

        return new KeyValue[]{new KeyValue(root.translateXProperty(), -10, interpolator),
                new KeyValue(root.translateYProperty(), -10, interpolator),
                new KeyValue(shadow.radiusProperty(), 35, interpolator),
                new KeyValue(shadow.offsetXProperty(), 10, interpolator),
                new KeyValue(shadow.offsetYProperty(), 10, interpolator)};
    }

    protected void init() {
        //Пустой метод для переопределения
    }

    protected KeyValue[] getStartFrame(DropShadow shadow, Interpolator interpolator) {
        return new KeyValue[]{
                new KeyValue(root.translateXProperty(), 0, interpolator),
                new KeyValue(root.translateYProperty(), 0, interpolator),
                new KeyValue(shadow.radiusProperty(), 0, interpolator),
                new KeyValue(shadow.offsetXProperty(), 0, interpolator),
                new KeyValue(shadow.offsetYProperty(), 0, interpolator)
        };
    }

    protected void update() {
        root.getChildren().clear();
        if (defaultNode != null)
            defaultNode.opacityProperty().unbind();
        defaultNode = getDefault();
        Utils.setAnchorProperty(defaultNode);
        root.getChildren().add(defaultNode);
    }

    protected abstract void onClick(MouseEvent mouseEvent);


    protected abstract Node getDefault();


}
