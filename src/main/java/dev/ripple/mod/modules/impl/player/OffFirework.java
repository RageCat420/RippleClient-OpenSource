package dev.ripple.mod.modules.impl.player;

import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.movement.ElytraFly;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class OffFirework extends Module {
	public static OffFirework INSTANCE;

	public OffFirework() {
		super("OffFirework", Category.Player);
		setChinese("放烟花");
		INSTANCE = this;
	}

	public final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap", true));

	@Override
	public void onEnable() {
		if (nullCheck()) {
			disable();
			return;
		}
		off();
		disable();
	}
	public void off() {
		ElytraFly.INSTANCE.fireworkTimer.reset();
		int firework;
		if (mc.player.getMainHandStack().getItem() == Items.FIREWORK_ROCKET) {
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
			//EntityUtil.swingHand(Hand.MAIN_HAND);
		} else if (inventory.getValue() && (firework = InventoryUtil.findItemInventorySlot(Items.FIREWORK_ROCKET)) != -1) {
			InventoryUtil.inventorySwap(firework, mc.player.getInventory().selectedSlot);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
			//EntityUtil.swingHand(Hand.MAIN_HAND);
			InventoryUtil.inventorySwap(firework, mc.player.getInventory().selectedSlot);
			EntityUtil.syncInventory();
		} else if ((firework = InventoryUtil.findItem(Items.FIREWORK_ROCKET)) != -1) {
			int old = mc.player.getInventory().selectedSlot;
			InventoryUtil.switchToSlot(firework);
			sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
			//EntityUtil.swingHand(Hand.MAIN_HAND);
			InventoryUtil.switchToSlot(old);
		}
	}
}

