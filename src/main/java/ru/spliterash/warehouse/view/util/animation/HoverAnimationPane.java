package ru.spliterash.warehouse.view.util.animation;

import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import ru.spliterash.warehouse.other.Utils;

public abstract class HoverAnimationPane extends AnimationPane {
    private SimpleDoubleProperty opacity;

    private Node hoverNode;

    @Override
    protected void init() {
        opacity = new SimpleDoubleProperty(1);
    }

    @Override
    protected KeyValue[] getStartFrame(DropShadow shadow, Interpolator interpolator) {
        return Utils.concat(super.getStartFrame(shadow, interpolator), new KeyValue[]{
                new KeyValue(opacity, 1, interpolator)
        });
    }

    @Override
    protected KeyValue[] getEndFrame(DropShadow shadow, Interpolator interpolator) {
        return Utils.concat(super.getEndFrame(shadow, interpolator), new KeyValue[]{
                new KeyValue(opacity, 0, interpolator)
        });
    }

    @Override
    protected void update() {
        super.update();
        if (hoverNode != null)
            hoverNode.opacityProperty().unbind();
        hoverNode = getHover();
        getRoot().getChildren().add(hoverNode);
        Utils.setAnchorProperty(hoverNode);
        opacity.set(1);
        getDefaultNode().opacityProperty().bind(opacity);
        hoverNode.opacityProperty().bind(new DoubleBinding() {
            {
                super.bind(opacity);
            }

            @Override
            protected double computeValue() {
                return 1D - opacity.get();
            }
        });
    }

    protected abstract Node getHover();
}
