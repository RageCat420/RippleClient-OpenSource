package dev.ripple.api.events.impl;

import dev.ripple.api.events.Event;

public class MouseScrollEvent extends Event {
    public double value;
    public MouseScrollEvent(double value) {
        super(Stage.Pre);
        this.value = value;
    }
}
