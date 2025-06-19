package dev.ripple.mod.modules.impl.player;

import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.eventbus.EventPriority;
import dev.ripple.api.events.impl.RotateEvent;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.Module;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Map;

public class AutoHeal extends Module {


    public static AutoHeal INSTANCE;
    private final SliderSetting delay = add(new SliderSetting("Delay", 3, 0, 10));
    public final BooleanSetting down = add(new BooleanSetting("Down", true));
    private final BooleanSetting onlyDamaged = add(new BooleanSetting("OnlyDamaged", true));
    private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyGround = add(new BooleanSetting("OnlyGround", true));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));
    private final Timer delayTimer = new Timer();

    public AutoHeal() {
        super("AutoHeal", Category.Player);
        setChinese("自动治疗");
        INSTANCE = this;
    }

    private boolean throwing = false;

    @Override
    public void onDisable() {
        throwing = false;
    }

    int count = 0;

    @Override
    public void onUpdate() {
        throwing = checkThrow();
        if (isThrowing() && delayTimer.passedMs(delay.getValueInt() * 20L) && (!onlyGround.getValue() || mc.player.isOnGround())) {
            count = getPotionCount() - 1;
            throwPotion();
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        count = getPotionCount();
    }

    @Override
    public String getInfo() {
        return String.valueOf(count);
    }

    public void throwPotion() {
        int oldSlot = mc.player.getInventory().selectedSlot;
        int newSlot;
        if (inventory.getValue() && (newSlot = findPotionInventorySlot()) != -1) {
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), 88));
            InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
            delayTimer.reset();
        } else if ((newSlot = findPotion()) != -1) {
            InventoryUtil.switchToSlot(newSlot);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), 88));
            InventoryUtil.switchToSlot(oldSlot);
            delayTimer.reset();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void RotateEvent(RotateEvent event) {
        if (!down.getValue()) return;
        if (isThrowing()) event.setPitch(88);
    }

    public boolean isThrowing() {
        return throwing;
    }

    public boolean checkThrow() {
        if (isOff()) return false;
        if (mc.currentScreen instanceof ChatScreen) return false;
        if (mc.currentScreen != null) return false;
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return false;
        }
        if (onlyDamaged.getValue() && mc.player.getHealth() >= 20) {
            return false;
        }
        return findPotion() != -1 || (inventory.getValue() && findPotionInventorySlot() != -1);
    }

    public static int getPotionCount() {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            ItemStack itemStack = entry.getValue();
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = itemStack.get(DataComponentTypes.POTION_CONTENTS).potion().get().value().getEffects();
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                    count = count + entry.getValue().getCount();
                }
            }
        }
        return count;
    }

    public static int findPotionInventorySlot() {
        for (int i = 0; i < 45; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = itemStack.get(DataComponentTypes.POTION_CONTENTS).potion().get().value().getEffects();
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                    return i < 9 ? i + 36 : i;
                }
            }
        }
        return -1;
    }

    public static int findPotion() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = InventoryUtil.getStackInSlot(i);
            if (Item.getRawId(itemStack.getItem()) != Item.getRawId(Items.SPLASH_POTION)) continue;
            List<StatusEffectInstance> effects = itemStack.get(DataComponentTypes.POTION_CONTENTS).potion().get().value().getEffects();
            for (StatusEffectInstance effect : effects) {
                if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                    return i;
                }
            }
        }
        return -1;
    }
}
