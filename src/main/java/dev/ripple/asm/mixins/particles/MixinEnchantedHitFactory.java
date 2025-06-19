package dev.ripple.asm.mixins.particles;

import dev.ripple.mod.modules.impl.render.Particles;
import net.minecraft.client.particle.DamageParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageParticle.EnchantedHitFactory.class)
public class MixinEnchantedHitFactory {
    @Inject(method = "createParticle*", at = @At("RETURN"), cancellable = true)
    private void injectCreateParticle(SimpleParticleType type, ClientWorld world, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        if (Particles.INSTANCE.isOn() && Particles.INSTANCE.crit.getValue() && Particles.INSTANCE.attack.getValue()) {
            Particle particle = cir.getReturnValue();
            particle.red /= 0.3f;
            particle.green /= 0.8f;
            cir.setReturnValue(particle);
        }
    }
}