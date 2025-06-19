package dev.ripple.asm.mixins.particles;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Particle.class)
public abstract class MixinParticle {
    @Shadow protected float alpha;
    @Shadow protected float blue;
    @Shadow protected float green;
    @Shadow protected float red;

    @Shadow
    public abstract Particle scale(float scale);
    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;
    @Shadow
    protected double velocityX;
    @Shadow
    protected double velocityY;
    @Shadow
    protected double velocityZ;

    @Shadow
    public abstract void setColor(float red, float green, float blue);

    @Shadow
    protected void setAlpha(float alpha) {
    }
}