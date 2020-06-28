package ru.spliterash.warehouse.view.util.animation;

import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import lombok.Getter;

@Getter
public class CancelableAnimationWrapper<T extends Animation> {
    private final SimpleBooleanProperty isRunNow = new SimpleBooleanProperty(false);
    private final T[] animations;

    @SafeVarargs
    public CancelableAnimationWrapper(ObservableValue<Boolean> property, T... animations) {
        this.animations = animations;
        property.addListener(this::onBooleanChange);

    }

    public void onBooleanChange(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        for (T animation : animations) {
            DoubleProperty rate = animation.rateProperty();
            if (newValue) {
                rate.set(1);
            } else {
                rate.set(-1);
            }
            animation.play();
        }
    }
}
