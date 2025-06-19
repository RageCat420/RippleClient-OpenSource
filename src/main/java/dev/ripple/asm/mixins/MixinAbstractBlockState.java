package dev.ripple.asm.mixins;

import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.render.Ambience;
import dev.ripple.mod.modules.impl.render.XRay;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.ripple.api.utils.Wrapper.mc;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState {
    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    public void getLuminanceHook(CallbackInfoReturnable<Integer> cir) {
        if (Module.nullCheck()) return;
        if (mc.player.portalManager == null || !mc.player.portalManager.isInPortal()) {
            if (XRay.INSTANCE.isOn()) {
                cir.setReturnValue(15);
            } else if (Ambience.INSTANCE.customLuminance.getValue()) {
                cir.setReturnValue(Ambience.INSTANCE.luminance.getValueInt());
            }
        }
    }
}