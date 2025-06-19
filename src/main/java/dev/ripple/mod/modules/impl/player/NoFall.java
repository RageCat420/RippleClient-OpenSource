package dev.ripple.mod.modules.impl.player;

import dev.ripple.api.events.impl.MoveEvent;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.asm.accessors.IPlayerMoveC2SPacket;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.exploit.BowBomb;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Box;

public class NoFall extends Module {
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.SpoofGround));
    private final SliderSetting distance = add(new SliderSetting("Distance", 3.0f, 0.0f, 8.0f, 0.1));
    private boolean triggered = false;
    private boolean sendSpoofPacket = false;
    private final Timer grimTimer = new Timer();

    public NoFall() {
        super("NoFall", Category.Player);
        setChinese("无摔伤");
    }

    public enum Mode {
        SpoofGround, Grim
    }

    @Override
    public void onEnable() {
        triggered = false;
        sendSpoofPacket = false;
        grimTimer.reset();
    }

    @Override
    public String getInfo() {
        return mode.getValue().toString();
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (nullCheck()) return;
        if (!grimTimer.passedMs(500)) return;
        if (mode.is(Mode.SpoofGround)) return;
        if (mc.player.isOnGround()) triggered = false;
        if (mc.player.isFallFlying()) return;
        if (triggered) return;
        if (mc.player.fallDistance >= distance.getValue()) {
            Box expandedBox = new Box(mc.player.getBoundingBox().minX, mc.player.getBoundingBox().minY - 2.0, mc.player.getBoundingBox().minZ, mc.player.getBoundingBox().maxX, mc.player.getBoundingBox().maxY, mc.player.getBoundingBox().maxZ);
            if (!mc.world.isSpaceEmpty(expandedBox)) {
                sendSpoofPacket = true;
                triggered = true;
            }
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            grimTimer.reset();
        }
    }

    @EventHandler
    public void onPacketSent(PacketEvent.Sent event) {
        if (mode.is(Mode.Grim) && sendSpoofPacket && event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            sendSpoofPacket = false;
            //0.000000001f
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()) + 0.000000001f, packet.getZ(mc.player.getZ()), packet.getYaw(mc.player.getYaw()) + 1337f, packet.getPitch(mc.player.getPitch()), packet.isOnGround()));
            CommandManager.sendChatMessage("[NoFall] Grim spoof attempted.");
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (mode.is(Mode.Grim)) return;
		/*
		for (ItemStack is : mc.player.getArmorItems()) {
			if (is.getItem() == Items.ELYTRA) {
				return;
			}
		}
		 */
        if (mc.player.isFallFlying()) return;
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (mc.player.fallDistance >= (float) this.distance.getValue() && !BowBomb.send) {
                ((IPlayerMoveC2SPacket) packet).setOnGround(true);
            }
        }
    }
}
