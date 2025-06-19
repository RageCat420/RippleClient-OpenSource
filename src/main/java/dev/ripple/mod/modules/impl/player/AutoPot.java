package dev.ripple.mod.modules.impl.player;

import dev.ripple.Ripple;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.AntiCheat;
import dev.ripple.mod.modules.settings.impl.BindSetting;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

import java.util.List;

public class AutoPot extends Module {

    public static AutoPot INSTANCE;
    private final SliderSetting delay = add(new SliderSetting("Delay", 5, 0, 10).setSuffix("s"));
    private final BooleanSetting speed =
            add(new BooleanSetting("Speed", true));
    private final BooleanSetting resistance =
            add(new BooleanSetting("Resistance", true));
    private final BooleanSetting strengh =
            add(new BooleanSetting("Strengh", true));
    private final BooleanSetting slowFalling =
            add(new BooleanSetting("SlowFalling", true));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    private final BindSetting speedKey =
            add(new BindSetting("SpeedKey",-1));
    private final BindSetting strenghKey =
            add(new BindSetting("StrenghKey",-1));
    private final BindSetting resistancehKey =
            add(new BindSetting("ResistanceKey",-1));
    private final Timer delayTimer = new Timer();
    private boolean throwing = false;

    public AutoPot() {
        super("AutoPot", Category.Player);
        setChinese("自动药水");
        INSTANCE = this;
    }

    public static int findPotionInventorySlot(RegistryEntry<StatusEffect> targetEffect) {
        for (int i = 0; i < 45; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = itemStack.get(DataComponentTypes.POTION_CONTENTS).potion().get().value().getEffects();
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == targetEffect) {
                    return i < 9 ? i + 36 : i;
                }
            }
        }
        return -1;
    }

    public static int findPotion(RegistryEntry<StatusEffect> targetEffect) {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = InventoryUtil.getStackInSlot(i);
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = itemStack.get(DataComponentTypes.POTION_CONTENTS).potion().get().value().getEffects();
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == targetEffect) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        throwing = false;
    }

    @Override
    public void onUpdate() {
        if (!onlyGround.getValue() || mc.player.isOnGround() && !mc.world.isAir(new BlockPosX(mc.player.getPos().add(0, -1, 0)))) {
            if (speed.getValue() && !mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                throwing = checkThrow(StatusEffects.SPEED);
                if (isThrow() && delayTimer.passedMs(delay.getValue() * 1000)) {
                    throwPotion(StatusEffects.SPEED);
                    return;
                }
            }
            if (resistance.getValue() && (!mc.player.hasStatusEffect(StatusEffects.RESISTANCE) || mc.player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() < 2)) {
                throwing = checkThrow(StatusEffects.RESISTANCE);
                if (isThrow() && delayTimer.passedMs(delay.getValue() * 1000)) {
                    throwPotion(StatusEffects.RESISTANCE);
                }
            }
            if (strengh.getValue() && (!mc.player.hasStatusEffect(StatusEffects.STRENGTH) || mc.player.getStatusEffect(StatusEffects.STRENGTH).getAmplifier() < 2)) {
                throwing = checkThrow(StatusEffects.STRENGTH);
                if (isThrow() && delayTimer.passedMs(delay.getValue() * 1000)) {
                    throwPotion(StatusEffects.STRENGTH);
                }
            }
            if (speedKey.isPressed()) {
                throwPotion(StatusEffects.SPEED);
            }
            if (strenghKey.isPressed()) {
                throwPotion(StatusEffects.STRENGTH);
            }
            if (resistancehKey.isPressed()) {
                throwPotion(StatusEffects.RESISTANCE);
            }
        }
    }

    public void throwPotion(RegistryEntry<StatusEffect> targetEffect) {
        int oldSlot = mc.player.getInventory().selectedSlot;
        int newSlot;
        if (inventory.getValue() && (newSlot = findPotionInventorySlot(targetEffect)) != -1) {
            Ripple.ROTATION.snapAt(Ripple.ROTATION.rotationYaw, 90);
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Ripple.ROTATION.rotationYaw, 90));
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
            if (AntiCheat.INSTANCE.snapBack.getValue()) {
                Ripple.ROTATION.snapBack();
            }
            delayTimer.reset();
        } else if ((newSlot = findPotion(targetEffect)) != -1) {
            Ripple.ROTATION.snapAt(Ripple.ROTATION.rotationYaw, 90);
            InventoryUtil.switchToSlot(newSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, Ripple.ROTATION.rotationYaw, 90));
            InventoryUtil.switchToSlot(oldSlot);
            if (AntiCheat.INSTANCE.snapBack.getValue()) {
                Ripple.ROTATION.snapBack();
            }
            delayTimer.reset();
        }
    }

    public boolean isThrow() {
        return throwing;
    }

    public boolean checkThrow(RegistryEntry<StatusEffect> targetEffect) {
        if (isOff()) return false;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof UIScreen) && !(mc.currentScreen instanceof GameMenuScreen)) {
            return false;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return false;
        }
        return findPotion(targetEffect) != -1 || (inventory.getValue() && findPotionInventorySlot(targetEffect) != -1);
    }
}
