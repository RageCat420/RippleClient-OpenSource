package dev.ripple.asm.mixins.particles;

import dev.ripple.api.utils.render.ColorUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.render.Particles;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Random;

@Mixin(TotemParticle.class)
public abstract class MixinTotemParticle extends MixinParticle {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, CallbackInfo ci) {
        if (Particles.INSTANCE.isOff() || !Particles.INSTANCE.totem.getValue()) return;
        this.scale(Particles.INSTANCE.scaleT.getValueFloat());
        this.velocityX *= Particles.INSTANCE.velocityT.getValue() / 100;
        this.velocityY *= Particles.INSTANCE.velocityT.getValue() / 100;
        this.velocityZ *= Particles.INSTANCE.velocityT.getValue() / 100;
        Random random = new Random();
        Color color = ColorUtil.fadeColor(Particles.INSTANCE.colorT.getValue(), Particles.INSTANCE.color2T.getValue(), random.nextDouble());
        if (color != null) {
            setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
            setAlpha(color.getAlpha() / 255f);
        }
    }
}