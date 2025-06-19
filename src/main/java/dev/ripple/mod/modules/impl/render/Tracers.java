package dev.ripple.mod.modules.impl.render;

import dev.ripple.api.utils.render.Render3DUtil;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

public class Tracers extends Module {
	private final ColorSetting player = add(new ColorSetting("Player", new Color(255, 255, 255, 100)).injectBoolean(true));
	private final ColorSetting wither = add(new ColorSetting("Wither", new Color(255, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting warden = add(new ColorSetting("Warden", new Color(255, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting dragon = add(new ColorSetting("Dragon", new Color(255, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting item = add(new ColorSetting("Item", new Color(255, 255, 255, 100)).injectBoolean(true));
	private final ColorSetting chest = add(new ColorSetting("Chest", new Color(255, 255, 255, 100)).injectBoolean(false));
	private final ColorSetting enderChest = add(new ColorSetting("EnderChest", new Color(255, 100, 255, 100)).injectBoolean(false));
	private final ColorSetting shulkerBox = add(new ColorSetting("ShulkerBox", new Color(15, 255, 255, 100)).injectBoolean(false));
	public Tracers() {
		super("Tracers", Category.Render);
		setChinese("追踪线");
	}

    @Override
	public void onRender3D(MatrixStack matrixStack) {
		if (nullCheck()) return;
		boolean prev_bob = mc.options.getBobView().getValue();
		mc.options.getBobView().setValue(false);
		if (item.booleanValue || player.booleanValue || wither.booleanValue || warden.booleanValue) {
			for (Entity entity : mc.world.getEntities()) {
				if (entity instanceof ItemEntity && item.booleanValue) {
					drawLine(entity.getPos(), item.getValue());
				} else if (entity instanceof PlayerEntity && player.booleanValue && entity != mc.player) {
					drawLine(entity.getPos(), player.getValue());
				} else if (entity instanceof WitherEntity && wither.booleanValue) {
					drawLine(entity.getPos(), wither.getValue());
				} else if (entity instanceof WardenEntity && warden.booleanValue) {
					drawLine(entity.getPos(), warden.getValue());
				} else if (entity instanceof EnderDragonEntity && dragon.booleanValue) {
					drawLine(entity.getPos(), dragon.getValue());
				}
			}
		}
		ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
		for (BlockEntity blockEntity : blockEntities) {
			if (blockEntity instanceof ChestBlockEntity && chest.booleanValue) {
				drawLine(blockEntity.getPos().toCenterPos(), chest.getValue());
			} else if (blockEntity instanceof EnderChestBlockEntity && enderChest.booleanValue) {
				drawLine(blockEntity.getPos().toCenterPos(), enderChest.getValue());
			} else if (blockEntity instanceof ShulkerBoxBlockEntity && shulkerBox.booleanValue) {
				drawLine(blockEntity.getPos().toCenterPos(), shulkerBox.getValue());
			}
		}
		mc.options.getBobView().setValue(prev_bob);
	}


	private void drawLine(Vec3d pos, Color color) {
		Render3DUtil.drawLine(pos, mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(true)).add(Vec3d.fromPolar(mc.player.getPitch(mc.getRenderTickCounter().getTickDelta(true)), mc.player.getYaw(mc.getRenderTickCounter().getTickDelta(true))).multiply(0.2)), color);
	}
}
