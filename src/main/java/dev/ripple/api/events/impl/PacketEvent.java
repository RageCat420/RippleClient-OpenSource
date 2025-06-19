package dev.ripple.api.events.impl;

import dev.ripple.api.events.Event;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends Event {

    private final Packet<?> packet;
    public PacketEvent(Packet<?> packet, Stage stage) {
        super(stage);
        this.packet = packet;
    }
    public <T extends Packet<?>> T getPacket() {
        return (T) packet;
    }
    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet, Stage.Pre);
        }
    }
    public static class Sent extends PacketEvent {
        public Sent(Packet<?> packet) {
            super(packet, Stage.Post);
        }
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet, Stage.Pre);
        }
    }
}
