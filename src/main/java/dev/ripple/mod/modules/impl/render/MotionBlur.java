package dev.ripple.mod.modules.impl.render;

import cn.noryea.motionblur.config.MotionBlurConfig;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.SliderSetting;

public class MotionBlur extends Module {
    public static MotionBlur INSTANCE;

    private final SliderSetting strength = add(new SliderSetting("Strength", 50, 0, 100));

    public MotionBlur() {
        super("MotionBlur", Category.Render);
        INSTANCE = this;
    }
    
    @Override
    public void onUpdate() {
        MotionBlurConfig.setMotionBlurAmount(strength.getValueInt());
    }
}