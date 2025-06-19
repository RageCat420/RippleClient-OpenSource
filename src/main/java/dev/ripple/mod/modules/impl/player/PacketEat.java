package dev.ripple.mod.modules.impl.player;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class PacketEat extends Module {
	public static PacketEat INSTANCE;
	private final BooleanSetting deSync =
			add(new BooleanSetting("DeSync", false));
	public PacketEat() {
		super("PacketEat", Category.Player);
		setChinese("发包进食");
		INSTANCE = this;
	}

    @Override
    public void onUpdate() {
		if (deSync.getValue() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().getComponents().contains(DataComponentTypes.FOOD)){
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
		}
	}

	@EventHandler
	public void onPacket(PacketEvent.Send event) {
		if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && mc.player.getActiveItem().getItem().getComponents().contains(DataComponentTypes.FOOD)) {
			event.cancel();
		}
	}
}