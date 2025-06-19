package dev.ripple.asm.mixins.particles;

import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.render.Particles;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(ExplosionLargeParticle.class)
public abstract class MixinExplosionLargeParticle extends MixinParticle {
    @Shadow @Final private SpriteProvider spriteProvider;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(ClientWorld world, double x, double y, double z, double d, SpriteProvider spriteProvider, CallbackInfo ci) {
        if (Particles.INSTANCE.isOff() || !Particles.INSTANCE.explosion.getValue()) return;
        this.scale(Particles.INSTANCE.scaleE.getValueFloat());
        Color color = Particles.INSTANCE.colorE.getValue();
        if (color != null) {
            setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
            setAlpha(color.getAlpha() / 255f);
        }
    }
}