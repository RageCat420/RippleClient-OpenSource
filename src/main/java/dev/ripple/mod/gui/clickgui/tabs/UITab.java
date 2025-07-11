package dev.ripple.mod.gui.clickgui.tabs;

import dev.ripple.Ripple;
import dev.ripple.api.utils.math.Animation;
import dev.ripple.mod.gui.clickgui.components.Component;
import dev.ripple.mod.gui.clickgui.components.impl.ModuleComponent;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.UI;
import dev.ripple.core.impl.GuiManager;
import dev.ripple.api.utils.render.Render2DUtil;
import dev.ripple.api.utils.render.TextUtil;
import dev.ripple.mod.gui.clickgui.UIScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;


public class UITab extends Tab {
	protected String title;
	protected final boolean drawBorder = true;
	private Module.Category category = null;
	protected final ArrayList<ModuleComponent> children = new ArrayList<>();

	public UITab(String title, int x, int y) {
		this.title = title;
		this.x = Ripple.CONFIG.getInt(title + "_x", x);
		this.y = Ripple.CONFIG.getInt(title + "_y", y);
		this.width = 98;
		this.mc = MinecraftClient.getInstance();
	}

	public UITab(Module.Category category, int x, int y) {
		this(category.name(), x, y);
		this.category = category;
	}
	public ArrayList<ModuleComponent> getChildren() {
		return children;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		this.title = title;
	}

	public final int getX() {
		return x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return y;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final int getWidth() {
		return width;
	}

	public final void setWidth(int width) {
		this.width = width;
	}

	public final int getHeight() {
		return height;
	}

	public final void setHeight(int height) {
		this.height = height;
	}

	public final boolean isGrabbed() {
		return (GuiManager.currentGrabbed == this);
	}

	public final void addChild(ModuleComponent component) {
		this.children.add(component);
	}

	boolean popped = true;

	@Override
	
	
	public void update(double mouseX, double mouseY) {
		onMouseClick(mouseX, mouseY);
		if (popped) {
			int tempHeight = 1;
			for (ModuleComponent child : children) {
				tempHeight += (child.getHeight());
			}
			this.height = tempHeight;
			int i = defaultHeight;
			for (ModuleComponent child : this.children) {
				child.update(i, mouseX, mouseY);
				i += child.getHeight();
			}
		}
	}

	public void onMouseClick(double mouseX, double mouseY) {
		if (GuiManager.currentGrabbed == null) {
			if (mouseX >= (x) && mouseX <= (x + width)) {
				if (mouseY >= (y + 1) && mouseY <= (y + 14)) {
					if (UIScreen.clicked) {
						GuiManager.currentGrabbed = this;
					}
					else if (UIScreen.rightClicked) {
						popped = !popped;
						UIScreen.rightClicked = false;
						Component.sound();
					}
				}
			}
		}
	}

	public double currentHeight = 0;
	Animation animation = new Animation();

	@Override
	
	
	public void draw(DrawContext drawContext, float partialTicks, Color color) {
		int tempHeight = 1;
		for (ModuleComponent child : children) {
			tempHeight += (child.getHeight());
		}
		this.height = tempHeight;

		MatrixStack matrixStack = drawContext.getMatrices();
		currentHeight = animation.get(height);
		if (drawBorder) {
			if (UI.INSTANCE.barEnd.booleanValue) {
				Render2DUtil.drawRectVertical(matrixStack, x, y - 1, width, 15, UI.INSTANCE.bar.getValue(), UI.INSTANCE.barEnd.getValue());
			} else {
				Render2DUtil.drawRect(matrixStack, x, y - 1, width, 15, UI.INSTANCE.bar.getValue());
			}
/*			Render2DUtil.drawRect(matrixStack, x, y - 1 + 15, width, 1, new Color(38, 38, 38));*/
			if (popped) Render2DUtil.drawRect(matrixStack, x, y + 14, width, (int) currentHeight, UI.INSTANCE.background.getValue());
		}
		if (popped) {
			int i = defaultHeight;
			for (Component child : children) {
				child.draw(i, drawContext, partialTicks, color, false);
				i += child.getHeight();
			}
		}
		//TextUtil.drawString(drawContext, this.title, x + width / 2d - TextUtil.getWidth(title) / 2, y + 3, new Color(255, 255, 255));
		TextUtil.drawString(drawContext, this.title, x + 4, y + 3, new Color(255, 255, 255));
	}
}
