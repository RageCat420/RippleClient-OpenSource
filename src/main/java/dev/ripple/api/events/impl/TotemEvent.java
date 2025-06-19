package dev.ripple.api.events.impl;

import dev.ripple.api.events.Event;
import net.minecraft.entity.player.PlayerEntity;

public class TotemEvent extends Event {
    private final PlayerEntity player;

    public TotemEvent(PlayerEntity player) {
        super(Stage.Post);
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}