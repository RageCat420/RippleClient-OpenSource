package dev.ripple.mod.modules.impl.client;

import com.terraformersmc.modmenu.gui.ModsScreen;
import dev.ripple.Ripple;
import dev.ripple.core.impl.GuiManager;
import dev.ripple.api.utils.math.Easing;
import dev.ripple.api.utils.math.FadeUtils;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.gui.clickgui.components.Component;
import dev.ripple.mod.gui.clickgui.components.impl.ModuleComponent;
import dev.ripple.mod.gui.clickgui.tabs.UITab;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.Setting;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.TitleScreen;

import java.awt.*;

public class UI extends Module {
    public static UI INSTANCE;
    private final EnumSetting<Pages> page = add(new EnumSetting<>("Page", Pages.General));
    public final EnumSetting<Type> uiType = add(new EnumSetting<>("UIType", Type.Old, () -> page.getValue() == Pages.Element));
    public final BooleanSetting activeBox = add(new BooleanSetting("ActiveBox", true, () -> page.getValue() == Pages.Element));
    public final BooleanSetting center = add(new BooleanSetting("Center", false, () -> page.getValue() == Pages.Element));
    public final BooleanSetting showDrawn = add(new BooleanSetting("ShowDrawn", true, () -> page.is(Pages.Element)));
    public final ColorSetting bind = add(new ColorSetting("Bind", new Color(255, 255, 255), () -> page.getValue() == Pages.Element).injectBoolean(false));
    public final ColorSetting gear = add(new ColorSetting("Gear", new Color(255, 255, 255), () -> page.getValue() == Pages.Element).injectBoolean(true));

    public final BooleanSetting chinese = add(new BooleanSetting("Chinese", false, () -> page.getValue() == Pages.General));
    public final BooleanSetting font = add(new BooleanSetting("Font", true, () -> page.getValue() == Pages.General));
    public final BooleanSetting maxFill = add(new BooleanSetting("MaxFill", false, () -> page.getValue() == Pages.General));
    public final BooleanSetting sound = add(new BooleanSetting("Sound", true, () -> page.getValue() == Pages.General));
    public final SliderSetting height = add(new SliderSetting("Height", 15, 10, 20, 1, () -> page.getValue() == Pages.General));
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("EnableAnim", Mode.Pull, () -> page.getValue() == Pages.General));
    public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 200, 0, 1000, 1, () -> page.getValue() == Pages.General));
    public final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.QuadInOut, () -> page.getValue() == Pages.General));

    public final ColorSetting color = add(new ColorSetting("Main", new Color(0, 0, 193, 77), () -> page.getValue() == Pages.Color));
    public final ColorSetting mainEnd = add(new ColorSetting("MainEnd", new Color(0, 0, 255, 130), () -> page.getValue() == Pages.Color).injectBoolean(false));
    public final ColorSetting mainHover = add(new ColorSetting("Hover", new Color(0, 0, 191, 124), () -> page.getValue() == Pages.Color));
    public final ColorSetting bar = add(new ColorSetting("Bar", new Color(0, 0, 191, 106), () -> page.getValue() == Pages.Color));
    public final ColorSetting barEnd = add(new ColorSetting("BarEnd", new Color(0, 0, 255, 130), () -> page.getValue() == Pages.Color).injectBoolean(false));
    public final ColorSetting disableText = add(new ColorSetting("DisableText", new Color(0, 0, 255, 255), () -> page.getValue() == Pages.Color));
    public final ColorSetting enableText = add(new ColorSetting("EnableText", new Color(0, 0, 255, 255), () -> page.getValue() == Pages.Color));
    public final ColorSetting enableTextS = add(new ColorSetting("EnableText2", new Color(0, 0, 63, 255), () -> page.getValue() == Pages.Color));
    public final ColorSetting module = add(new ColorSetting("Module", new Color(0, 0, 50, 112), () -> page.getValue() == Pages.Color));
    public final ColorSetting moduleHover = add(new ColorSetting("ModuleHover", new Color(0, 0, 191, 122), () -> page.getValue() == Pages.Color));
    public final ColorSetting setting = add(new ColorSetting("Setting", new Color(0, 0, 50, 112), () -> page.getValue() == Pages.Color));
    public final ColorSetting settingHover = add(new ColorSetting("SettingHover", new Color(0, 0, 191, 112), () -> page.getValue() == Pages.Color));
    public final ColorSetting background = add(new ColorSetting("Background", new Color(0, 0, 10, 112), () -> page.getValue() == Pages.Color));

    public UI() {
        super("UI", Category.Client);
        setChinese("菜单");
        INSTANCE = this;
    }

    public static final FadeUtils fade = new FadeUtils(300);

    @Override
    public void onUpdate() {
		/*
		if (chinese.getValue()) {
			font.setValue(false);
		}
		 */
        if (!(mc.currentScreen instanceof UIScreen)) {
            disable();
        }
        Ripple.MODULE.modules.forEach(module1 -> {
            for (Setting settings : module1.getSettings()) {
                if (settings == drawnSetting) {
                    drawnSetting.hide = !showDrawn.getValue();
                    break;
                }
            }
        });
    }

    int lastHeight;

    @Override
    public void onEnable() {
        if (lastHeight != height.getValueInt()) {
            for (UITab tab : Ripple.GUI.tabs) {
                for (Component component : tab.getChildren()) {
                    if (component instanceof ModuleComponent moduleComponent) {
                        for (Component settingComponent : moduleComponent.getSettingsList()) {
                            settingComponent.setHeight(height.getValueInt());
                            settingComponent.defaultHeight = height.getValueInt();
                        }
                    }
                    component.setHeight(height.getValueInt());
                    component.defaultHeight = height.getValueInt();
                }
            }
            lastHeight = height.getValueInt();
        }
        fade.reset();
        if (nullCheck()) {
            //disable();
            return;
        }
        mc.setScreen(GuiManager.uiScreen);
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof UIScreen) {
            mc.setScreen(nullCheck() ? new ModsScreen(new TitleScreen()) : null);
        }
    }

    public enum Mode {
        Scale, Pull, None
    }

    private enum Pages {
        General, Color, Element
    }

    public enum Type {
        Old, New
    }
}