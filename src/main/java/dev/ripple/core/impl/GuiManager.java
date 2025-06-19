package dev.ripple.core.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ripple.Ripple;
import dev.ripple.api.utils.Wrapper;
import dev.ripple.api.utils.math.FadeUtils;
import dev.ripple.api.utils.render.Snow;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.gui.clickgui.components.impl.ModuleComponent;
import dev.ripple.mod.gui.clickgui.tabs.UITab;
import dev.ripple.mod.gui.clickgui.tabs.Tab;
import dev.ripple.mod.gui.elements.ArmorHUD;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.UI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GuiManager implements Wrapper {

	public final ArrayList<UITab> tabs = new ArrayList<>();
	public static final UIScreen uiScreen = new UIScreen();
	public final ArmorHUD armorHud;
	public static Tab currentGrabbed = null;
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private int mouseX;
	private int mouseY;

	public GuiManager() {

		armorHud = new ArmorHUD();

		int xOffset = 30;
		for (Module.Category category : Module.Category.values()) {
			UITab tab = new UITab(category, xOffset, 50);
			for (Module module : Ripple.MODULE.modules) {
				if (module.getCategory() == category) {
					ModuleComponent button = new ModuleComponent(tab, module);
					tab.addChild(button);
				}
			}
			tabs.add(tab);
			xOffset += tab.getWidth() + 5;
		}
	}
	
	public Color getColor() {
		return UI.INSTANCE.color.getValue();
	}
	
	public void onUpdate() {
		if (isClickGuiOpen()) {
			for (UITab tab : tabs) {
				tab.update(mouseX, mouseY);
			}
			armorHud.update(mouseX, mouseY);
		}
	}

	
	
	public void draw(int x, int y, DrawContext drawContext, float tickDelta) {
		MatrixStack matrixStack = drawContext.getMatrices();
		boolean mouseClicked = UIScreen.clicked;
		mouseX = x;
		mouseY = y;
		if (!mouseClicked) {
			currentGrabbed = null;
		}
		if (currentGrabbed != null) {
			currentGrabbed.moveWindow((lastMouseX - mouseX), (lastMouseY - mouseY));
		}
		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;
		RenderSystem.enableCull();
		matrixStack.push();
		//matrixStack.scale((float) ClickGui.size, (float) ClickGui.size, 1);
		armorHud.draw(drawContext, tickDelta, getColor());
		double quad = UI.fade.ease(FadeUtils.Ease.In2);
		if (quad < 1) {
			switch (UI.INSTANCE.mode.getValue()) {
				case Pull -> {
					quad = 1 - quad;
					matrixStack.translate(0, -100 * quad, 0);
				}
				case Scale -> matrixStack.scale((float) quad, (float) quad, 1);
			}
		}
		for (UITab tab : tabs) {
			tab.draw(drawContext, tickDelta, getColor());
		}
		matrixStack.pop();
	}

	public boolean isClickGuiOpen() {
		return mc.currentScreen instanceof UIScreen;
	}

	public static final ArrayList<Snow> snows = new ArrayList<>(){
		{
			Random random = new Random();
			for (int i = 0; i < 100; ++i) {
				for (int y = 0; y < 3; ++y) {
					add(new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1));
				}
			}
		}
	};
}
