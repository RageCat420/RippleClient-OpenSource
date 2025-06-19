package dev.ripple.mod.modules.impl.render;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.MouseScrollEvent;
import dev.ripple.mod.modules.settings.impl.BindSetting;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.utils.math.FadeUtils;
import dev.ripple.mod.modules.Module;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class CameraClip extends Module {
    public static CameraClip INSTANCE;
    public CameraClip() {
        super("CameraClip", Category.Render);
        setChinese("视角穿墙");
        INSTANCE = this;
    }

    public final SliderSetting distance = add(new SliderSetting("Distance", 4f, 1f, 20f));
    public final SliderSetting animateTime = add(new SliderSetting("AnimationTime", 200, 0, 1000));
    private final BooleanSetting noFront = add(new BooleanSetting("NoFront", false));
    private final BooleanSetting scroll = add(new BooleanSetting("Scroll", true).setParent());
    private final BindSetting sb = add(new BindSetting("Bind", GLFW.GLFW_KEY_LEFT_CONTROL, scroll::isOpen));
    private final BooleanSetting noFirst = add(new BooleanSetting("NoFirst", true, scroll::isOpen));
    private final SliderSetting sensitivity = add(new SliderSetting("Sensitivity", 1, 0.01, 5, 0.01, scroll::isOpen));
    private final FadeUtils animation = new FadeUtils(300);
    boolean first = false;
    public float d = 4f;

    @Override
    public void onEnable() {
        d = distance.getValueFloat();
    }

    @EventHandler
    public void onMouseScroll(MouseScrollEvent event) {
        if (nullCheck()) return;
        if (mc.currentScreen != null || !scroll.getValue() || !sb.isPressed()) return;
        if (noFirst.getValue() && mc.options.getPerspective() == Perspective.FIRST_PERSON) return;
        if (sensitivity.getValueFloat() > 0) {
            d -= (float) (event.value * 0.25 * (sensitivity.getValueFloat() * d));
            event.cancel();
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && noFront.getValue())
            mc.options.setPerspective(Perspective.FIRST_PERSON);
        animation.setLength(animateTime.getValueInt());
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            if (!first) {
                first = true;
                animation.reset();
            }
        } else {
            if (first) {
                first = false;
                animation.reset();
            }
        }
    }

    public double getDistance() {
        double quad = mc.options.getPerspective() == Perspective.FIRST_PERSON ? 1 - animation.easeOutQuad() : animation.easeOutQuad();
        return 1d + ((d - 1d) * quad);
    }
}
