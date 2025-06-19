package dev.ripple.mod.modules.impl.render;

import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.api.utils.math.MathUtil;
import dev.ripple.api.utils.render.Render3DUtil;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.asm.accessors.IEntity;
import dev.ripple.mod.modules.Module;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

public class ESP extends Module {
	private final ColorSetting player = add(new ColorSetting("Player", new Color(255, 255, 255, 100)).injectBoolean(true));
	private final ColorSetting wither = add(new ColorSetting("Wither", new Color(255, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting warden = add(new ColorSetting("Warden", new Color(255, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting dragon = add(new ColorSetting("Dragon", new Color(255, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting item = add(new ColorSetting("Item", new Color(255, 255, 255, 100)).injectBoolean(true));
	private final ColorSetting chest = add(new ColorSetting("Chest", new Color(255, 255, 255, 100)).injectBoolean(false));
	private final ColorSetting enderChest = add(new ColorSetting("EnderChest", new Color(255, 100, 255, 100)).injectBoolean(false));
	private final ColorSetting shulkerBox = add(new ColorSetting("ShulkerBox", new Color(15, 255, 255, 100)).injectBoolean(false));
	public ESP() {
		super("ESP", Category.Render);
		setChinese("透视");
	}

    @Override
	public void onRender3D(MatrixStack matrixStack) {
		if (item.booleanValue || player.booleanValue) {
			for (Entity entity : mc.world.getEntities()) {
				if (entity instanceof ItemEntity && item.booleanValue) {
					Color color = this.item.getValue();
					Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderY, entity.getY(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), mc.getRenderTickCounter().getTickDelta(true)))), color, false, true);
				} else if (entity instanceof PlayerEntity && player.booleanValue) {
					Color color = this.player.getValue();
					Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderY, entity.getY(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), mc.getRenderTickCounter().getTickDelta(true)))).expand(0, 0.1, 0), color, false, true);
				} else if (entity instanceof WitherEntity && wither.booleanValue) {
					Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderY, entity.getY(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), mc.getRenderTickCounter().getTickDelta(true)))).expand(0, 0.1, 0), wither.getValue(), false, true);
				} else if (entity instanceof WardenEntity && warden.booleanValue) {
					Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderY, entity.getY(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), mc.getRenderTickCounter().getTickDelta(true)))).expand(0, 0.1, 0), warden.getValue(), false, true);
				} else if (entity instanceof EnderDragonEntity && dragon.booleanValue) {
					Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderY, entity.getY(), mc.getRenderTickCounter().getTickDelta(true)), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), mc.getRenderTickCounter().getTickDelta(true)))).expand(0, 0.1, 0), dragon.getValue(), false, true);
				}
			}
		}
		ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
		for (BlockEntity blockEntity : blockEntities) {
			if (blockEntity instanceof ChestBlockEntity && chest.booleanValue) {
				Box box = new Box(blockEntity.getPos());
				Render3DUtil.draw3DBox(matrixStack, box, chest.getValue());
			} else if (blockEntity instanceof EnderChestBlockEntity && enderChest.booleanValue) {
				Box box = new Box(blockEntity.getPos());
				Render3DUtil.draw3DBox(matrixStack, box, enderChest.getValue());
			} else if (blockEntity instanceof ShulkerBoxBlockEntity && shulkerBox.booleanValue) {
				Box box = new Box(blockEntity.getPos());
				Render3DUtil.draw3DBox(matrixStack, box, shulkerBox.getValue());
			}
		}
	}
}
