package dev.ripple.mod.gui.clickgui.components.impl;

import dev.ripple.Ripple;
import dev.ripple.api.utils.math.Animation;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.gui.clickgui.tabs.UITab;
import dev.ripple.mod.modules.impl.client.UI;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.core.impl.GuiManager;
import dev.ripple.api.utils.render.Render2DUtil;
import dev.ripple.api.utils.render.TextUtil;
import dev.ripple.mod.gui.clickgui.components.Component;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
public class EnumComponent extends Component {
	private final EnumSetting<?> setting;
	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.getAsBoolean();
		}
		return true;
	}
	public EnumComponent(UITab parent, EnumSetting<?> enumSetting) {
		super();
		this.parent = parent;
		setting = enumSetting;
	}

	private boolean hover = false;

	
	
	public void update(int offset, double mouseX, double mouseY) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if ((mouseX >= ((parentX + 2)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
			hover = true;
			if (GuiManager.currentGrabbed == null && isVisible()) {
				if (UIScreen.clicked) {
					UIScreen.clicked = false;
					setting.increaseEnum();
					sound();
				}
				if (UIScreen.rightClicked) {
					setting.popped = !setting.popped;
					UIScreen.rightClicked = false;
					sound();
				}
			}
		} else {
			hover = false;
		}

		if (GuiManager.currentGrabbed == null && isVisible() && UIScreen.clicked) {
			int cy = parentY + offset - 1 + (defaultHeight - 2) - 2;
			if (setting.popped) {
				for (Object o : setting.getValue().getDeclaringClass().getEnumConstants()) {
					if (mouseX >= parentX && mouseX <= parentX + parentWidth && mouseY >= TextUtil.getHeight() / 2 + cy && mouseY < TextUtil.getHeight() + TextUtil.getHeight() / 2 + cy) {
						setting.setEnumValue(String.valueOf(o));
						UIScreen.clicked = false;
						sound();
						break;
					}
					cy += (int) TextUtil.getHeight();
				}
			}
		}
		y = 0;
		if (setting.popped) {
			for (Object ignored : setting.getValue().getDeclaringClass().getEnumConstants()) {
				y += (int) TextUtil.getHeight();
			}
			setHeight(defaultHeight + y);
		} else {
			setHeight(defaultHeight);
		}
	}

	@Override
	public int getCurrentHeight() {
		return (int) (defaultHeight + popHeightAnimation.get(y));
	}
	int y = 0;
	public double currentY = 0;
	public Animation popHeightAnimation = new Animation();
	@Override
	
	
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		y = 0;
		if (setting.popped) {
			for (Object ignored : setting.getValue().getDeclaringClass().getEnumConstants()) {
				y += (int) TextUtil.getHeight();
			}
			setHeight(defaultHeight + y);
		} else {
			setHeight(defaultHeight);
		}
		int x = parent.getX();
		int y = parent.getY() + offset - 2;
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();

		if (UI.INSTANCE.mainEnd.booleanValue) {
			Render2DUtil.drawRectHorizontal(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - (UI.INSTANCE.maxFill.getValue() ? 0 : 1), hover ? UI.INSTANCE.mainHover.getValue() : Ripple.GUI.getColor(), UI.INSTANCE.mainEnd.getValue());
		} else {
			Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - (UI.INSTANCE.maxFill.getValue() ? 0 : 1), hover ? UI.INSTANCE.mainHover.getValue() : Ripple.GUI.getColor());
		}
		TextUtil.drawString(drawContext, setting.getName() + ": " + setting.getValue().name(), x + 4, y + getTextOffsetY(), -1);
		TextUtil.drawString(drawContext, setting.popped ? "-" : "+", x + width - 11, y + getTextOffsetY(), new Color(255, 255, 255).getRGB());


		if (setting.popped) {
			currentY = animation.get(1);
		} else {
			currentY = animation.get(0);
		}
		double cy = (parent.getY() + offset - 1 + (defaultHeight - 2)) - 2;
		if (currentY > 0.04) {
			for (Object o : setting.getValue().getDeclaringClass().getEnumConstants()) {

				String s = o.toString();

				TextUtil.drawString(drawContext, s, width / 2d - TextUtil.getWidth(s) / 2d + 2.0f + x, TextUtil.getHeight() / 2d + (cy), setting.getValue().name().equals(s) ? new Color(255, 255, 255, (int) (currentY * 255)).getRGB() : new Color(120, 120, 120, (int) (currentY * 255)).getRGB());
				cy += TextUtil.getHeight() * currentY;
			}
		}
		return true;
	}
}