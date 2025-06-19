package dev.ripple.asm.mixins;

import dev.ripple.mod.modules.impl.player.Freecam;
import dev.ripple.mod.modules.impl.player.freelook.FreeLook;
import dev.ripple.mod.modules.impl.render.CameraClip;
import dev.ripple.mod.modules.impl.render.MotionCamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Unique
    private double lastMouseX = 0.0d;

    @Unique
    private double lastMouseY = 0.0d;

    @Unique
    private float lastYaw;

    @Unique
    private float lastPitch;

    @Unique
    private float f5yaw;

    @Unique
    private float f5pitch;

    @Inject(method = {"update"}, at = {@At("HEAD")})
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (MotionCamera.INSTANCE.isOn()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                double currentMouseX = client.mouse.getX();
                double currentMouseY = client.mouse.getY();
                double mouseDeltaX = currentMouseX - this.lastMouseX;
                double mouseDeltaY = currentMouseY - this.lastMouseY;
                float yawChange = (float) (mouseDeltaX * 0.1f);
                float pitchChange = (float) (mouseDeltaY * 0.1f);
                if (client.options.getPerspective() == Perspective.FIRST_PERSON) {
                    float currentYaw = client.player.getYaw();
                    float currentPitch = MathHelper.clamp(client.player.getPitch(), -90.0f, 90.0f);
                    this.lastYaw = currentYaw;
                    this.lastPitch = currentPitch;
                    this.f5yaw = currentYaw;
                    this.f5pitch = currentPitch;
                } else if (client.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
                    this.f5yaw += yawChange;
                    this.f5pitch += pitchChange;
                    this.f5yaw = MathHelper.wrapDegrees(this.f5yaw);
                    this.f5pitch = MathHelper.clamp(this.f5pitch, -90.0f, 90.0f);
                    client.player.setYaw(this.f5yaw);
                    client.player.setPitch(this.f5pitch);
                }
                this.lastMouseX = currentMouseX;
                this.lastMouseY = currentMouseY;
            }
        }
    }

    @ModifyArgs(method = {"update"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void setRotationHook(Args args) {
        if (MotionCamera.INSTANCE.isOn()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
                args.set(0, Float.valueOf(this.lastYaw));
                args.set(1, Float.valueOf(this.lastPitch));
            }
        }

        if (Freecam.INSTANCE.isOn())
            args.setAll(Freecam.INSTANCE.getFakeYaw(), Freecam.INSTANCE.getFakePitch());
    }

    @Shadow
    protected abstract float clipToSpace(float desiredCameraDistance);

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V", ordinal = 0))
    private void modifyCameraDistance(Args args) {
        if (CameraClip.INSTANCE.isOn()) {
            args.set(0, -clipToSpace((float) CameraClip.INSTANCE.getDistance()));
        }
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float f, CallbackInfoReturnable<Float> cir) {
        if (CameraClip.INSTANCE.isOn()) {
            cir.setReturnValue((float) CameraClip.INSTANCE.getDistance());
        }
    }

    @Shadow
    private boolean thirdPerson;


    @Shadow protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (Freecam.INSTANCE.isOn()) {
            this.thirdPerson = true;
        }
    }

//    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
//    private void setRotationHook(Args args) {
//        if(Freecam.INSTANCE.isOn())
//            args.setAll(Freecam.INSTANCE.getFakeYaw(), Freecam.INSTANCE.getFakePitch());
//    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosHook(Args args) {
        if(Freecam.INSTANCE.isOn())
            args.setAll(Freecam.INSTANCE.getFakeX(), Freecam.INSTANCE.getFakeY(), Freecam.INSTANCE.getFakeZ());
    }

    @Unique
    private float lastUpdate;

    @Inject(method = "update", at = @At("HEAD"))
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        var camera = FreeLook.INSTANCE.getCameraState();

        if (camera.doLock) {
            camera.lookYaw = MathHelper.wrapDegrees(camera.lookYaw);
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void modifyRotationArgs(Args args) {
        var camera = FreeLook.INSTANCE.getCameraState();

        if (camera.doLock) {
            var yaw = camera.lookYaw;
            var pitch = camera.lookPitch;

            if (MinecraftClient.getInstance().options.getPerspective().isFrontView()) {
                yaw -= 180;
                pitch = -pitch;
            }

            args.set(0, yaw);
            args.set(1, pitch);
        } else if (camera.doTransition) {
            var delta = (getCurrentTime() - lastUpdate);

            var steps = 1.2f;
            var speed = 2f;
            var yawDiff = camera.lookYaw - camera.originalYaw();
            var pitchDiff = camera.lookPitch - camera.originalPitch();
            var yawStep = speed * (yawDiff * steps);
            var pitchStep = speed * (pitchDiff * steps);
            var yaw = MathHelper.stepTowards(camera.lookYaw, camera.originalYaw(), yawStep * delta);
            var pitch = MathHelper.stepTowards(camera.lookPitch, camera.originalPitch(), pitchStep * delta);

            camera.lookYaw = yaw;
            camera.lookPitch = pitch;

            args.set(0, yaw);
            args.set(1, pitch);

            camera.doTransition =
                    (int) camera.originalYaw() != (int) camera.lookYaw ||
                            (int) camera.originalPitch() != (int) camera.lookPitch;
        }

        lastUpdate = getCurrentTime();
    }

    @Unique
    private float getCurrentTime() {
        return (float) (System.nanoTime() * 0.00000001);
    }
}
