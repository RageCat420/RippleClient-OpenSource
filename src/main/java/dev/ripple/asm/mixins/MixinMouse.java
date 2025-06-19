package dev.ripple.asm.mixins;

import dev.ripple.Ripple;
import dev.ripple.api.events.impl.MouseScrollEvent;
import dev.ripple.api.events.impl.MouseUpdateEvent;
import dev.ripple.mod.gui.clickgui.UIScreen;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.ripple.api.utils.Wrapper.mc;

@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        int key = -(button + 2);
        if (mc.currentScreen instanceof UIScreen && action == 1 && Ripple.MODULE.setBind(key)) {
            return;
        }
        if (action == 1) {
            Ripple.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            Ripple.MODULE.onKeyReleased(key);
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = new MouseScrollEvent(vertical);
        Ripple.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "updateMouse", at = @At("RETURN"))
    private void updateHook(CallbackInfo ci) {
        Ripple.EVENT_BUS.post(new MouseUpdateEvent());
    }
}
