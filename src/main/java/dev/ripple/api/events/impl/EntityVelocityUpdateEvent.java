package dev.ripple.api.events.impl;

import dev.ripple.api.events.Event;

public class EntityVelocityUpdateEvent extends Event {
    public EntityVelocityUpdateEvent() {
        super(Stage.Pre);
    }
}
