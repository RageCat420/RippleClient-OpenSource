package dev.ripple.mod.gui.clickgui.components;

import dev.ripple.api.utils.Wrapper;
import dev.ripple.api.utils.math.AnimateUtil;
import dev.ripple.api.utils.math.Animation;
import dev.ripple.mod.gui.clickgui.tabs.UITab;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.UI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.awt.*;

public abstract class Component implements Wrapper {
	public int defaultHeight = 16;
	protected UITab parent;
	private int height = defaultHeight;

	public Animation animation = new Animation();
	public Component() {
	}

	public boolean isVisible() {
		return true;
	}
	
	public int getHeight()
	{
		if (!isVisible()) {
			return 0;
		}
		return height;
	}
	public int getCurrentHeight() {
		return getHeight();
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public UITab getParent()
	{
		return parent;
	}
	
	public void setParent(UITab parent)
	{
		this.parent = parent;
	}

	public abstract void update(int offset, double mouseX, double mouseY);
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		return false;
	}
	public double currentOffset = 0;

	public double getTextOffsetY() {
		return (defaultHeight - Wrapper.mc.textRenderer.fontHeight) / 2D + (UI.INSTANCE.maxFill.getValue() ? 2 : 1);
	}

	public static double animate(double current, double endPoint, double speed) {
		if (speed >= 1) return endPoint;
		if (speed == 0) return current;
		return AnimateUtil.thunder(current, endPoint, speed);
	}
	public static void sound() {
		if (UI.INSTANCE.sound.getValue() && !Module.nullCheck()) {
			mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.BLOCKS, (float) 100f, 1.9f);
		}
	}
}
