package dev.ripple.mod.modules.impl.misc;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.combat.Criticals;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class AttackCDTweaks extends Module {

    public AttackCDTweaks() {
        super("AttackCDTweaks", Category.Misc);
        setChinese("攻击冷却修改");
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof HandSwingC2SPacket || packet instanceof PlayerInteractEntityC2SPacket && Criticals.getInteractType((PlayerInteractEntityC2SPacket) packet) == Criticals.InteractType.ATTACK) {
            mc.player.resetLastAttackedTicks();
        }
    }
}
