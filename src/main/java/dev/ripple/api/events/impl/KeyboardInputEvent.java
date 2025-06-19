package dev.ripple.api.events.impl;

import dev.ripple.api.events.Event;

public class KeyboardInputEvent extends Event {
    public KeyboardInputEvent() {
        super(Stage.Pre);
    }
}
