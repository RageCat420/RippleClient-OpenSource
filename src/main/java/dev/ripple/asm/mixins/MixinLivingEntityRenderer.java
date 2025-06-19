package dev.ripple.asm.mixins;

import dev.ripple.Ripple;
import dev.ripple.api.utils.math.MathUtil;
import dev.ripple.api.utils.render.Render2DUtil;
import dev.ripple.core.impl.GuiManager;
import dev.ripple.core.impl.RotationManager;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.ClientSetting;
import dev.ripple.mod.modules.impl.render.Chams;
import dev.ripple.mod.modules.impl.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

import static dev.ripple.api.utils.Wrapper.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private LivingEntity lastEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalHeadYaw;
    @Unique
    private float originalBodyYaw;
    @Unique
    private float originalPitch;

    @Unique
    private float originalPrevYaw;
    @Unique
    private float originalPrevHeadYaw;
    @Unique
    private float originalPrevBodyYaw;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Module.nullCheck()) return;
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && ClientSetting.INSTANCE.rotations.getValue()) {
            originalYaw = livingEntity.getYaw();
            originalHeadYaw = livingEntity.headYaw;
            originalBodyYaw = livingEntity.bodyYaw;
            originalPitch = livingEntity.getPitch();
            originalPrevYaw = livingEntity.prevYaw;
            originalPrevHeadYaw = livingEntity.prevHeadYaw;
            originalPrevBodyYaw = livingEntity.prevBodyYaw;

            livingEntity.setYaw(RotationManager.getRenderYawOffset());
            livingEntity.headYaw = RotationManager.getRotationYawHead();
            livingEntity.bodyYaw = RotationManager.getRenderYawOffset();
            livingEntity.setPitch(RotationManager.getRenderPitch());
            livingEntity.prevYaw = RotationManager.getPrevRenderYawOffset();
            livingEntity.prevHeadYaw = RotationManager.getPrevRotationYawHead();
            livingEntity.prevBodyYaw = RotationManager.getPrevRenderYawOffset();
            livingEntity.prevPitch = RotationManager.getPrevPitch();
        }
        lastEntity = livingEntity;
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Module.nullCheck()) return;
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && ClientSetting.INSTANCE.rotations.getValue()) {
            livingEntity.setYaw(originalYaw);
            livingEntity.headYaw = originalHeadYaw;
            livingEntity.bodyYaw = originalBodyYaw;
            livingEntity.setPitch(originalPitch);
            livingEntity.prevYaw = originalPrevYaw;
            livingEntity.prevHeadYaw = originalPrevHeadYaw;
            livingEntity.prevBodyYaw = originalPrevBodyYaw;
            livingEntity.prevPitch = originalPitch;
        }
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void modifyColor(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Chams module = Chams.INSTANCE;
        if (module.isOff()) return;
        Color color = new Color(-1);
        if (livingEntity instanceof SlimeEntity && module.slimes.booleanValue) {
            color = module.slimes.getValue();
        } else if (livingEntity instanceof PlayerEntity && module.players.booleanValue) {
            color = module.players.getValue();
            if (Ripple.FRIEND.isFriend((PlayerEntity) livingEntity)) {
                color = module.friends.getValue();
            }
        } else if ((livingEntity instanceof VillagerEntity || livingEntity instanceof WanderingTraderEntity)
                && module.villagers.booleanValue) {
            color = module.villagers.getValue();
        } else if (livingEntity instanceof AnimalEntity && module.animals.booleanValue) {
            color = module.animals.getValue();
        } else if (livingEntity instanceof MobEntity && module.mobs.booleanValue) {
            color = module.mobs.getValue();
        }
        args.set(4, color.getRGB());
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderHook(Args args) {
        if (Module.nullCheck()) return;

        float alpha = -1f;

        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.antiPlayerCollision.getValue() && lastEntity != mc.player && lastEntity instanceof PlayerEntity pl && !pl.isInvisible())
            alpha = MathUtil.clamp((float) (mc.player.squaredDistanceTo(lastEntity.getPos()) / 3f) + 0.2f, 0f, 1f);

        if (alpha != -1) args.set(4, Render2DUtil.applyOpacity(0x26FFFFFF, alpha));
    }
}
