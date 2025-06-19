package dev.ripple.mod.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ripple.Ripple;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.modules.impl.client.HUD;
import dev.ripple.core.impl.GuiManager;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.render.ColorUtil;
import dev.ripple.api.utils.render.Render2DUtil;
import dev.ripple.api.utils.render.TextUtil;
import dev.ripple.mod.gui.clickgui.tabs.Tab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ArmorHUD extends Tab {

	public ArmorHUD() {
		this.width = 80;
		this.height = 34;
		this.x = (int) Ripple.CONFIG.getFloat("armor_x", 0);
		this.y = (int) Ripple.CONFIG.getFloat("armor_y", 200);
	}

	@Override
	public void update(double mouseX, double mouseY) {
		if (GuiManager.currentGrabbed == null && HUD.INSTANCE.armor.getValue()) {
			if (mouseX >= (x) && mouseX <= (x + width)) {
				if (mouseY >= (y) && mouseY <= (y + height)) {
					if (UIScreen.clicked) {
						GuiManager.currentGrabbed = this;
					}
				}
			}
		}
	}

	@Override
	public void draw(DrawContext drawContext, float partialTicks, Color color) {
		MatrixStack matrixStack = drawContext.getMatrices();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		if (HUD.INSTANCE.armor.getValue()) {
			if (Ripple.GUI.isClickGuiOpen()) {
				Render2DUtil.drawRect(drawContext.getMatrices(), x, y, width, height, new Color(0, 0, 0, 70));
			}
			int xOff = 0;
			if (mc.player == null) return;
			for (ItemStack armor : mc.player.getInventory().armor) {
				xOff += 20;

				if (armor.isEmpty()) continue;
				matrixStack.push();
				int damage = EntityUtil.getDamagePercent(armor);
				int yOffset = height / 2;
				drawContext.drawItem(armor, this.x + width - xOff, this.y + yOffset);
				drawContext.drawItemInSlot(mc.textRenderer, armor, this.x + width - xOff, this.y + yOffset);
				TextUtil.drawStringScale(drawContext, damage + "%",
                        (float) (x + width + 2 - xOff),
                        (float) (y + yOffset - mc.textRenderer.fontHeight / 4d),
						ColorUtil.fadeColor(new Color(196, 0, 0), new Color(0, 227, 0), damage / 100f).getRGB(), 0.5F);
/*				drawContext.drawText(mc.textRenderer,
						String.valueOf(damage),
						x + width + 8 - xOff - mc.textRenderer.getWidth(String.valueOf(damage)) / 2,
						y + yOffset - mc.textRenderer.fontHeight - 2,
						new Color((int) (255f * (1f - ((float) damage / 100f))), (int) (255f * ((float) damage / 100f)), 0).getRGB(),
						true);*/
				matrixStack.pop();
			}
		}
	}
}
