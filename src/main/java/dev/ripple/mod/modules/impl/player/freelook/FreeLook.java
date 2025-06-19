package dev.ripple.mod.modules.impl.player.freelook;

import dev.ripple.Ripple;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.Render3DEvent;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.option.Perspective;

import java.util.concurrent.CompletableFuture;

public class FreeLook extends Module {
    public static FreeLook INSTANCE;
    public FreeLook() {
        super("FreeLook", Category.Player);
        setChinese("自由视角");
        camera = new CameraState();
        INSTANCE = this;
        Ripple.EVENT_BUS.subscribe(new FreeLookUpdate());
    }

    private final BooleanSetting togglePer = add(new BooleanSetting("TogglePerspective", true));
    private final EnumSetting<Perspective> per = add(new EnumSetting<>("PerspectiveMode", Perspective.THIRD_PERSON_BACK, togglePer::getValue));
    private final SliderSetting delay = add(new SliderSetting("Delay", 150, 0, 1000, togglePer::getValue));

    private final CameraState camera;
    private Perspective pre = Perspective.FIRST_PERSON;
    public boolean origin = true;

    public CameraState getCameraState() {
        return camera;
    }

    @Override
    public void onEnable() {
        pre = mc.options.getPerspective();
        origin = false;
    }

    public class FreeLookUpdate {
        @EventHandler
        public void onRender3D(Render3DEvent event) {
            CameraState camera = getCameraState();
            var doLock = isOn() && !camera.doLock;
            var doUnlock = !isOn() && camera.doLock;

            if (doLock) {
                if (!camera.doTransition) {
                    camera.lookYaw = camera.originalYaw();
                    camera.lookPitch = camera.originalPitch();
                }
                if (togglePer.getValue()) mc.options.setPerspective(per.getValue());

                camera.doLock = true;
            }

            if (doUnlock) {
                camera.doLock = false;
                camera.doTransition = true;

                camera.transitionInitialYaw = camera.lookYaw;
                camera.transitionInitialPitch = camera.lookPitch;
            }

            if (isOff() && !origin) {
                origin = true;
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(delay.getValueInt());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    mc.options.setPerspective(pre);
                });
            }
        }
    }
}
