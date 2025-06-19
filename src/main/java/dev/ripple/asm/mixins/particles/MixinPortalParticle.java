package dev.ripple.asm.mixins.particles;

import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.render.Particles;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(PortalParticle.class)
public abstract class MixinPortalParticle extends MixinParticle {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, CallbackInfo ci) {
        if (Particles.INSTANCE.isOff() || !Particles.INSTANCE.portal.getValue()) return;
        this.scale(Particles.INSTANCE.scaleP.getValueFloat());
        this.velocityX *= Particles.INSTANCE.velocityP.getValue() / 100;
        this.velocityY *= Particles.INSTANCE.velocityP.getValue() / 100;
        this.velocityZ *= Particles.INSTANCE.velocityP.getValue() / 100;
        Color color = Particles.INSTANCE.colorP.getValue();
        if (color != null) {
            setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
            setAlpha(color.getAlpha() / 255f);
        }
    }
}