package dev.ripple.api.events.impl;

import dev.ripple.api.events.Event;

public class UpdateWalkingPlayerEvent extends Event {
    public UpdateWalkingPlayerEvent(Stage stage) {
        super(stage);
    }
}
