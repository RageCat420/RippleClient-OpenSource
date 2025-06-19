package dev.ripple.asm.mixins;

import dev.ripple.Ripple;
import dev.ripple.mod.modules.impl.render.MotionCamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
/* loaded from: smcamera.jar:asia/cuke/mixin/SmoothCameraMixin.class */
public class MotionCameraMixin {
    @Unique
    private double smoothCameraX = 0.0d;
    @Unique
    private double smoothCameraY = 0.0d;
    @Unique
    private double smoothCameraZ = 0.0d;
    @Unique
    private double targetX = 0.0d;
    @Unique
    private double targetY = 0.0d;
    @Unique
    private double targetZ = 0.0d;
    @Unique
    private final double smoothingFactor = 0.05d;

    @Unique
    private double getSmoothValue() {
        MinecraftClient mc = MinecraftClient.getInstance();
        double x = mc.player.getX() - mc.player.prevX;
        double z = mc.player.getZ() - mc.player.prevZ;
        double dist = Math.sqrt(x * x + z * z) / 1000.0;
        double div = 0.05 / 3600.0;
        float timer = Ripple.TIMER.get();
        final double speed = dist / div * timer;
        //return MotionCamera.INSTANCE.factor.getValueFloat() * speed / 20;
        return MotionCamera.INSTANCE.factor.getValueFloat();
    }

    @ModifyArgs(method = {"update"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosHook(Args args) {
        if (MotionCamera.INSTANCE.isOn()) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity clientPlayerEntity = client.player;
            if (clientPlayerEntity != null && client.options.getPerspective() != Perspective.THIRD_PERSON_BACK) {
                this.targetX = clientPlayerEntity.getX();
                this.targetY = clientPlayerEntity.getY() + 2.0d;
                this.targetZ = clientPlayerEntity.getZ();
                this.smoothCameraX = this.targetX;
                this.smoothCameraY = this.targetY;
                this.smoothCameraZ = this.targetZ;
            }
            if (clientPlayerEntity != null && client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
                this.targetX = clientPlayerEntity.getX();
                this.targetY = clientPlayerEntity.getY() + 2.0d;
                this.targetZ = clientPlayerEntity.getZ();
                this.smoothCameraX += (this.targetX - this.smoothCameraX) * getSmoothValue();
                this.smoothCameraY += (this.targetY - this.smoothCameraY) * getSmoothValue();
                this.smoothCameraZ += (this.targetZ - this.smoothCameraZ) * getSmoothValue();
                args.setAll(new Object[]{Double.valueOf(this.smoothCameraX), Double.valueOf(this.smoothCameraY), Double.valueOf(this.smoothCameraZ)});
            }
        }
    }
}