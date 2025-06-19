package dev.ripple.api.events.impl;

import dev.ripple.api.events.Event;

public class GameLeftEvent extends Event {
    public GameLeftEvent() {
        super(Stage.Post);
    }
}
