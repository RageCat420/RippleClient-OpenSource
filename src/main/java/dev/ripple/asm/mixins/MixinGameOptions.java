package dev.ripple.asm.mixins;

import dev.ripple.mod.modules.impl.render.CameraClip;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinGameOptions {
    @Inject(method = "setPerspective", at = @At("HEAD"))
    public void onSetPerspective(Perspective perspective, CallbackInfo ci) {
        if (CameraClip.INSTANCE == null) return;
        CameraClip.INSTANCE.d = CameraClip.INSTANCE.distance.getValueFloat();
    }
}