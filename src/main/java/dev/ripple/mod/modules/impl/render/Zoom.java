package dev.ripple.mod.modules.impl.render;

import dev.ripple.Ripple;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.MouseScrollEvent;
import dev.ripple.api.events.impl.Render3DEvent;
import dev.ripple.api.utils.math.Animation;
import dev.ripple.api.utils.math.Easing;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.mob.SilverfishEntity;

public class Zoom extends Module {
    public static Zoom INSTANCE;
    public double currentFov;
    private final SliderSetting animTime = add(new SliderSetting("AnimTime", 300, 0, 1000));
    public final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.CubicInOut));
    final SliderSetting fov = add(new SliderSetting("ZoomFov", 60, 10, 130, 1));
    private final BooleanSetting scroll = add(new BooleanSetting("Scroll", true).setParent());
    public final SliderSetting s = add(new SliderSetting("Sensitivity", 1, 0.01, 5, 0.01, scroll::isOpen));

    public Zoom() {
        super("Zoom", Category.Render);
        setChinese("放大");
        INSTANCE = this;
        Ripple.EVENT_BUS.subscribe(new ZoomAnim());
    }

    @Override
    public void onEnable() {
        target = fov.getValueFloat();
        if (nullCheck()) {
            disable();
        }
    }

    Animation animation = new Animation();

    public static boolean on = false;
    public double target = 60;

    @EventHandler
    private void onMouseScroll(MouseScrollEvent event) {
        if (nullCheck()) return;
        if (mc.currentScreen != null) return;
        if (!scroll.getValue()) return;
        event.cancel();
        target += event.value * 0.25 * (s.getValueFloat() * target);
        if (target < 1) {
            target = 1;
        }
        if (target > 130) {
            target = 130;
        }
        currentFov = target;
    }

    public class ZoomAnim {
        @EventHandler
        public void onRender3D(Render3DEvent event) {
            if (isOn()) {
                if (currentFov != target) {
                    currentFov = animation.get(target, animTime.getValueInt(), ease.getValue());
                }
                on = true;
            } else if (on) {
                currentFov = animation.get(0, animTime.getValueInt(), ease.getValue());
                if (currentFov == 0) {
                    on = false;
                }
            }
        }
    }
}
