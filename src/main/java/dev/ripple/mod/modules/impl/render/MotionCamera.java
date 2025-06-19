package dev.ripple.mod.modules.impl.render;

import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.SliderSetting;

public class MotionCamera extends Module {
    public static MotionCamera INSTANCE;
    public final SliderSetting factor = add(new SliderSetting("SmoothFactor", 0.05, 0.01, 1, 0.01));
    public MotionCamera() {
        super("MotionCamera", Category.Render);
        setChinese("运动相机");
        INSTANCE = this;
    }
}
