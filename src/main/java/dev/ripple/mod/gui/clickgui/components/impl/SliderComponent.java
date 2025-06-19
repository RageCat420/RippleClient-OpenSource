package dev.ripple.mod.gui.clickgui.components.impl;

import dev.ripple.core.impl.GuiManager;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.api.utils.render.Render2DUtil;
import dev.ripple.api.utils.render.TextUtil;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.gui.clickgui.components.Component;
import dev.ripple.mod.gui.clickgui.tabs.UITab;
import dev.ripple.mod.modules.impl.client.UI;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class SliderComponent extends Component {

	private final UITab parent;
	private double currentSliderPosition;
	final SliderSetting setting;

	public SliderComponent(UITab parent, SliderSetting setting) {
		super();
		this.parent = parent;
		this.setting = setting;
	}

	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.getAsBoolean();
		}
		return true;
	}

	private boolean clicked = false;
	private boolean hover = false;
	private boolean firstUpdate = true;

	@Override
	
	public void update(int offset, double mouseX, double mouseY) {
		if (firstUpdate || setting.update) {
			this.currentSliderPosition = (float) ((setting.getValue() - setting.getMinimum()) / setting.getRange());
			firstUpdate = false;
		}
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();

		if ((mouseX >= ((parentX)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
			hover = true;
			if (GuiManager.currentGrabbed == null && isVisible()) {
				if (UIScreen.clicked) {
					sound();
				}
				if (UIScreen.clicked || UIScreen.hoverClicked && clicked) {
					if (setting.isListening()) {
						setting.setListening(false);
						UIScreen.clicked = false;
					} else {
						clicked = true;
						UIScreen.hoverClicked = true;
						UIScreen.clicked = false;
						this.currentSliderPosition = (float) Math.min((mouseX - (parentX)) / (parentWidth - 4), 1f);
						this.currentSliderPosition = Math.max(0f, this.currentSliderPosition);
						this.setting.setValue((this.currentSliderPosition * this.setting.getRange()) + this.setting.getMinimum());
					}
				}
				if (UIScreen.rightClicked) {
					sound();
					setting.setListening(!setting.isListening());
					UIScreen.rightClicked = false;
				}
			}
		} else {
			clicked = false;
			hover = false;
		}
	}

	public double renderSliderPosition = 0;
	private final Timer timer = new Timer();
	boolean b;

	@Override
	
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		if (back) {
			setting.setListening(false);
		}
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		renderSliderPosition = animation.get(Math.floor((parentWidth - 2) * currentSliderPosition));
		float height = UI.INSTANCE.uiType.getValue() == UI.Type.New ? 1 : defaultHeight - (UI.INSTANCE.maxFill.getValue() ? 0 : 1);
		float y = UI.INSTANCE.uiType.getValue() == UI.Type.New ? (float) (parentY + offset + defaultHeight - 3) : (float) (parentY + offset - 1);
		if (UI.INSTANCE.mainEnd.booleanValue) {
			Render2DUtil.drawRectHorizontal(matrixStack, parentX + 1, y, (int) this.renderSliderPosition, height, hover ? UI.INSTANCE.mainHover.getValue() : color, UI.INSTANCE.mainEnd.getValue());
		} else {
			Render2DUtil.drawRect(matrixStack, parentX + 1, y, (int) this.renderSliderPosition, height, hover ? UI.INSTANCE.mainHover.getValue() : color);
		}
		if (this.setting == null) return true;
		if (setting.isListening()) {
			if (timer.passed(1000)) {
				b = !b;
				timer.reset();
			}
			TextUtil.drawString(drawContext, setting.temp + (b ? "_" : ""), parentX + 4,
					(float) (parentY + getTextOffsetY() + offset - 2), 0xFFFFFF);
		} else {
			String value;
			if (setting.getValueInt() == setting.getValue()) {
				value = String.valueOf(setting.getValueInt());
			} else {
				value = String.valueOf(this.setting.getValueFloat());
			}
			value = value + setting.getSuffix();
			TextUtil.drawString(drawContext, setting.getName(), (float) (parentX + 4),
					(float) (parentY + getTextOffsetY() + offset - 2), 0xFFFFFF);
			TextUtil.drawString(drawContext, value, parentX + parentWidth - TextUtil.getWidth(value) - 5,
					(float) (parentY + getTextOffsetY() + offset - 2), 0xFFFFFF);
		}
		return true;
	}
}

