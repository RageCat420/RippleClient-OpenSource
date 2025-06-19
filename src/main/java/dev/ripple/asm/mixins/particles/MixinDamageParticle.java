package dev.ripple.asm.mixins.particles;

import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.render.Particles;
import net.minecraft.client.particle.DamageParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(DamageParticle.class)
public abstract class MixinDamageParticle extends MixinParticle {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, CallbackInfo ci) {
        if (Particles.INSTANCE.isOff() || !Particles.INSTANCE.crit.getValue() || !Particles.INSTANCE.attack.getValue()) return;
        scale(Particles.INSTANCE.scaleC.getValueFloat());
        this.velocityX *= Particles.INSTANCE.velocityC.getValue() / 100;
        this.velocityY *= Particles.INSTANCE.velocityC.getValue() / 100;
        this.velocityZ *= Particles.INSTANCE.velocityC.getValue() / 100;
        Color color = Particles.INSTANCE.colorC.getValue();
        if (color != null) {
            setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
            setAlpha(color.getAlpha() / 255f);
        }
    }
    
    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void injectTick(CallbackInfo ci) {
        if (Particles.INSTANCE.isOff() || !Particles.INSTANCE.crit.getValue() || !Particles.INSTANCE.attack.getValue()) return;
        this.green /= 0.96f;
        this.blue /= 0.9f;
    }
}