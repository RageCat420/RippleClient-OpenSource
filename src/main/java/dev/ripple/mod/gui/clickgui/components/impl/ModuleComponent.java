package dev.ripple.mod.gui.clickgui.components.impl;

import dev.ripple.core.impl.GuiManager;
import dev.ripple.api.utils.math.Animation;
import dev.ripple.api.utils.math.FadeUtils;
import dev.ripple.api.utils.render.Render2DUtil;
import dev.ripple.api.utils.render.TextUtil;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.gui.clickgui.components.Component;
import dev.ripple.mod.gui.clickgui.tabs.UITab;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.UI;
import dev.ripple.mod.modules.settings.Setting;
import dev.ripple.mod.modules.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {

	private final Module module;
	private final UITab parent;
	private boolean popped = false;

	private int expandedHeight = defaultHeight;

	private final List<Component> settingsList = new ArrayList<>();
	public List<Component> getSettingsList() {
		return settingsList;
	}
	public ModuleComponent(UITab parent, Module module) {
		super();
		this.parent = parent;
		this.module = module;
		for (Setting setting : this.module.getSettings()) {
			Component c;
			if (setting.hide) {
				c = null;
			} else if (setting instanceof SliderSetting) {
				c = new SliderComponent(this.parent, (SliderSetting) setting);
			} else if (setting instanceof BooleanSetting) {
				c = new BooleanComponent(this.parent, (BooleanSetting) setting);
			} else if (setting instanceof BindSetting) {
				c = new BindComponent(this.parent, (BindSetting) setting);
			} else if (setting instanceof EnumSetting) {
				c = new EnumComponent(this.parent, (EnumSetting<?>) setting);
			} else if (setting instanceof ColorSetting) {
				c = new ColorComponents(this.parent, (ColorSetting) setting);
			} else if (setting instanceof StringSetting) {
				c = new StringComponent(this.parent, (StringSetting) setting);
			} else {
				c = null;
			}
			if (c != null)
				settingsList.add(c);
		}

		recalculateExpandedHeight();
	}

	boolean hovered = false;

	
	
	public void update(int offset, double mouseX, double mouseY) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();

		if (this.popped) {
			int i = offset + defaultHeight + 1;
			for (Component children : this.settingsList) {
				children.update(i, mouseX, mouseY);
				i += children.getHeight();
			}
		}

		hovered = ((mouseX >= parentX && mouseX <= (parentX + parentWidth)) && (mouseY >= parentY + offset && mouseY <= (parentY + offset + defaultHeight - 1)));
		if (hovered && GuiManager.currentGrabbed == null) {
			if (UIScreen.clicked) {
				UIScreen.clicked = false;
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
					sound();
					module.drawnSetting.toggleValue();
				} else {
					sound();
					module.toggle();
				}
			}

			if (UIScreen.rightClicked) {
				UIScreen.rightClicked = false;
				this.popped = !this.popped;
				sound();
			}
		}
		recalculateExpandedHeight();
		if (this.popped) {
			this.setHeight(expandedHeight);
		} else {
			this.setHeight(defaultHeight);
		}
	}

	public double currentWidth = 0;
	public Animation offsetAnimation = new Animation();
	public double currentPopHeight = 0;
	public Animation popHeightAnimation = new Animation();
	@Override
	
	
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		recalculateExpandedHeight();
		String text = module.getDisplayName();
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		currentOffset = offsetAnimation.get(offset);
		boolean scissor = UI.fade.ease(FadeUtils.Ease.Out) >= 1;
		if (scissor) {
			drawContext.enableScissor(parentX, (int) ((parentY + currentOffset + defaultHeight)), (parentX + parentWidth), mc.getWindow().getScaledHeight());
		}
		currentPopHeight = popHeightAnimation.get(popped ? (expandedHeight - defaultHeight) : 0);
		if (currentPopHeight > 0) {
			int i = (int) (currentOffset + defaultHeight + 1);
			if (scissor) {
				drawContext.enableScissor(parentX, (parentY + i - 1), (parentX + parentWidth), (int) ((parentY + currentOffset + defaultHeight + currentPopHeight)));
			}
			for (Component children : this.settingsList) {
				if (children.isVisible()) {
					children.draw(i, drawContext, partialTicks, color, !popped);
					i += children.getCurrentHeight();
				}
			}
			if (scissor) {
				drawContext.disableScissor();
			}
		}
		if (scissor) {
			drawContext.disableScissor();
		}
		currentWidth = animation.get(module.isOn() ? (parentWidth - 2D) : 0D);
		if (UI.INSTANCE.activeBox.getValue()) {
			if (UI.INSTANCE.mainEnd.booleanValue) {
				Render2DUtil.drawRectHorizontal(matrixStack, parentX + 1, (int) (parentY + currentOffset), (float) currentWidth, defaultHeight - (UI.INSTANCE.maxFill.getValue() ? 0 : 1), hovered ? UI.INSTANCE.mainHover.getValue() : UI.INSTANCE.color.getValue(), UI.INSTANCE.mainEnd.getValue());
			} else {
				Render2DUtil.drawRect(matrixStack, parentX + 1, (int) (parentY + currentOffset), (float) currentWidth, defaultHeight - (UI.INSTANCE.maxFill.getValue() ? 0 : 1), hovered ? UI.INSTANCE.mainHover.getValue() : UI.INSTANCE.color.getValue());
			}
		}
		if (module.isOff() || !UI.INSTANCE.activeBox.getValue())
			Render2DUtil.drawRect(matrixStack, parentX + 1, (int) (parentY + currentOffset), parentWidth - 2, defaultHeight - (UI.INSTANCE.maxFill.getValue() ? 0 : 1), hovered ? UI.INSTANCE.moduleHover.getValue() : UI.INSTANCE.module.getValue());
		if (hovered && InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			TextUtil.drawString(drawContext, "Drawn " + (module.drawnSetting.getValue() ? "§aOn" : "§cOff"), (float) (parentX + 4), (float) (parentY + getTextOffsetY() + currentOffset) - 1, -1);
		} else {
			if (UI.INSTANCE.center.getValue()) {
				TextUtil.drawString(drawContext, text, parentX + parentWidth / 2f - TextUtil.getWidth(text) / 2, (float) (parentY + getTextOffsetY() + currentOffset) - 1,
						module.isOn() ? UI.INSTANCE.enableText.getValue().getRGB() : UI.INSTANCE.disableText.getValue().getRGB());
			} else {
				TextUtil.drawString(drawContext, text, (float) (parentX + 4), (float) (parentY + getTextOffsetY() + currentOffset) - 1,
						module.isOn() ? UI.INSTANCE.enableText.getValue().getRGB() : UI.INSTANCE.disableText.getValue().getRGB());
			}
		}

		if (UI.INSTANCE.bind.booleanValue) {
			if (module.getBind().getKey() != -1) {
				String bindText = "[" + module.getBind().getBind() + "]";
				TextUtil.drawStringWithScale(drawContext, bindText, (UI.INSTANCE.center.getValue() ? (parentX + parentWidth / 2f - TextUtil.getWidth(text) / 2) : (parentX + 4)) + 1 + TextUtil.getWidth(text), (float) (parentY + getTextOffsetY() + currentOffset - TextUtil.getHeight() / 4), UI.INSTANCE.bind.getValue(), 0.5f);
			}
		}
		if (UI.INSTANCE.gear.booleanValue) {
			if (popped) {
				TextUtil.drawString(drawContext, "-", parentX + parentWidth - 11,
						parentY + getTextOffsetY() + currentOffset - 1, UI.INSTANCE.gear.getValue().getRGB());
			} else {
				TextUtil.drawString(drawContext, "+", parentX + parentWidth - 11,
						parentY + getTextOffsetY() + currentOffset - 1, UI.INSTANCE.gear.getValue().getRGB());
			}
		}
		return true;
	}

	public void recalculateExpandedHeight() {
		int height = defaultHeight;
		for (Component children : this.settingsList) {
			if (children != null && children.isVisible()) {
				height += children.getHeight();
			}
		}
		expandedHeight = height;
	}
}
