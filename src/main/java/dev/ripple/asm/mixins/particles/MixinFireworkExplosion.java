package dev.ripple.asm.mixins.particles;

import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.render.Particles;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(FireworksSparkParticle.Explosion.class)
public abstract class MixinFireworkExplosion extends MixinParticle {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ParticleManager particleManager, SpriteProvider spriteProvider, CallbackInfo ci) {
        if (Particles.INSTANCE.isOff() || !Particles.INSTANCE.rocket.getValue()) return;
        scale(Particles.INSTANCE.scaleR.getValueFloat());
        this.velocityX *= Particles.INSTANCE.velocityR.getValue() / 100;
        this.velocityY *= Particles.INSTANCE.velocityR.getValue() / 100;
        this.velocityZ *= Particles.INSTANCE.velocityR.getValue() / 100;
        Color color = Particles.INSTANCE.colorR.getValue();
        if (color != null) {
            setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
            setAlpha(color.getAlpha() / 255f);
        }
    }
}