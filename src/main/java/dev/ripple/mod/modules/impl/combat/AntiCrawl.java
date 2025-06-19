package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.Ripple;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.player.SpeedMine;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.util.math.BlockPos;

public class AntiCrawl extends Module {
	public static AntiCrawl INSTANCE;

	public AntiCrawl() {
		super("AntiCrawl", Category.Combat);
		setChinese("反趴下");
		INSTANCE = this;
	}
	private final BooleanSetting pre = add(new BooleanSetting("Pre", true));

	public boolean work = false;
	double[] xzOffset = new double[]{0, 0.3, -0.3};
	@Override
	public void onUpdate() {
		work = false;
		for (double offset : xzOffset) {
			for (double offset2 : xzOffset) {
				BlockPos pos = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.2, mc.player.getZ() + offset2);
				if (mc.player.isCrawling() || pre.getValue() && Ripple.BREAK.isMining(pos.down())) {
					if (canBreak(pos)) {
						SpeedMine.INSTANCE.mine(pos);
						work = true;
						return;
					}
				}
			}
		}
	}

	private boolean canBreak(BlockPos pos) {
		return (BlockUtil.getClickSideStrict(pos) != null || SpeedMine.getBreakPos().equals(pos)) && !SpeedMine.godBlocks.contains(mc.world.getBlockState(pos).getBlock()) && !mc.world.isAir(pos);
	}
}