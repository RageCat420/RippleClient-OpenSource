package dev.ripple.asm.mixins;

import dev.ripple.Ripple;
import dev.ripple.api.events.impl.TimerEvent;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public class MixinDynamic {
    @Shadow
    private float lastFrameDuration;

    @Inject(at = {@At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode = Opcodes.PUTFIELD, ordinal = 0)}, method = {"beginRenderTick(J)I"})
    public void onBeginRenderTick(long long_1, CallbackInfoReturnable<Integer> cir) {
        TimerEvent event = new TimerEvent();
        Ripple.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            if (event.isModified()) {
                lastFrameDuration *= event.get();
            } else {
                lastFrameDuration *= Ripple.TIMER.get();
            }
        }
    }
}
