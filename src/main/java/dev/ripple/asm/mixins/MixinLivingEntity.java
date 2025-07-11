package dev.ripple.asm.mixins;

import dev.ripple.Ripple;
import dev.ripple.api.events.Event;
import dev.ripple.api.events.impl.SprintEvent;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.exploit.AntiClimb;
import dev.ripple.mod.modules.impl.exploit.AntiEffects;
import dev.ripple.mod.modules.impl.movement.ElytraFly;
import dev.ripple.mod.modules.impl.movement.Glide;
import dev.ripple.mod.modules.impl.render.ViewModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.ripple.api.utils.Wrapper.mc;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"), require = 0)
    private boolean tickMovementHook(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        if (Glide.INSTANCE != null && Glide.INSTANCE.isOn() && Glide.INSTANCE.onlyFall.getValue()) return false;
        return instance.hasStatusEffect(effect);
    }

    @Inject(method = "isClimbing", at = @At("HEAD"), cancellable = true)
    public void isClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (Module.nullCheck()) return;
        if (AntiClimb.INSTANCE.isOff()) return;
        if (mc.player.isCrawling() && AntiClimb.INSTANCE.crawl.getValue()) return;
        //if (PearlPhase.INSTANCE.isOff() && AntiClimb.INSTANCE.phase.getValue()) return;
        cir.setReturnValue(false);
        cir.cancel();
    }

    @Final
    @Shadow
    private static EntityAttributeModifier SPRINTING_SPEED_BOOST;

    @Shadow
    @Nullable
    public EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getCustomInstance(attribute);
    }

    @Shadow
    public AttributeContainer getAttributes() {
        return null;
    }

    @Shadow
    public abstract void remove(RemovalReason reason);

    @Inject(method = {"getHandSwingDuration"}, at = {@At("HEAD")}, cancellable = true)
    private void getArmSwingAnimationEnd(final CallbackInfoReturnable<Integer> info) {
        if (ViewModel.INSTANCE.isOn() && ViewModel.INSTANCE.slowAnimation.getValue())
            info.setReturnValue(ViewModel.INSTANCE.slowAnimationVal.getValueInt());
    }

    @Unique
    private boolean previousElytra = false;

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = cir.getReturnValue();
        if (previousElytra && !elytra && ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce)) {
            cir.setReturnValue(ElytraFly.recastElytra(MinecraftClient.getInstance().player));
        }
        previousElytra = elytra;
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"), require = 0)
    private boolean travelEffectHook(LivingEntity instance, RegistryEntry<StatusEffect> effect) {
        if (AntiEffects.INSTANCE.isOn()) {
            if (effect == StatusEffects.SLOW_FALLING && AntiEffects.INSTANCE.slowFalling.getValue()) {
                return false;
            }
            if (effect == StatusEffects.LEVITATION && AntiEffects.INSTANCE.levitation.getValue()) {
                return false;
            }
        }
        return instance.hasStatusEffect(effect);
    }

    @Inject(method = {"setSprinting"}, at = {@At("HEAD")}, cancellable = true)
    public void setSprintingHook(boolean sprinting, CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            SprintEvent event = new SprintEvent(Event.Stage.Pre);
            Ripple.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
                sprinting = event.isSprint();
                super.setSprinting(sprinting);
                EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.id());
                if (sprinting) {
                    entityAttributeInstance.addTemporaryModifier(SPRINTING_SPEED_BOOST);
                }
            }
        }
    }
}
