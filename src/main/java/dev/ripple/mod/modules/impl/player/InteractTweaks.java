package dev.ripple.mod.modules.impl.player;

import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.CompletableFuture;

public class InteractTweaks extends Module {

    public static InteractTweaks INSTANCE;
    public final BooleanSetting noEntityTrace = add(new BooleanSetting("NoEntityTrace", true).setParent());
    public final BooleanSetting onlyPickaxe = add(new BooleanSetting("OnlyPickaxe", true, noEntityTrace::isOpen));
    public final BooleanSetting multiTask = add(new BooleanSetting("MultiTask", true));
    public final BooleanSetting respawn = add(new BooleanSetting("Respawn", true).setParent());
    private final BooleanSetting autoKit = add(new BooleanSetting("AutoKit", false, respawn::isOpen).setParent());
    //private final SliderSetting kitDelay = add(new SliderSetting("KitDelay", 500, 0, 5000, () -> autoKit.isOpen() && autoKit.visibility.getAsBoolean()).setSuffix("ms"));
    private final StringSetting command = add(new StringSetting("Command", "kit ripple", () -> autoKit.isOpen() && autoKit.visibility.getAsBoolean()));
    private final BooleanSetting noAbort = add(new BooleanSetting("NoMineAbort", false));
    private final BooleanSetting noReset = add(new BooleanSetting("NoMineReset", false));
    private final BooleanSetting noDelay = add(new BooleanSetting("NoMineDelay", false));
    private final BooleanSetting pickaxeSwitch = add(new BooleanSetting("SwitchEat", false).setParent());
    private final BooleanSetting allowSword = add(new BooleanSetting("allowSword", true, pickaxeSwitch::isOpen));
    public final BooleanSetting ghostHand = add(new BooleanSetting("IgnoreBedrock", false));
    private final BooleanSetting reach = add(new BooleanSetting("Reach", false).setParent());
    public final SliderSetting bDistance = add(new SliderSetting("BlockDistance", 5, 0, 15, 0.1, reach::isOpen));
    public final SliderSetting eDistance = add(new SliderSetting("EntityDistance", 3.1, 0, 15, 0.1, reach::isOpen));
    private final SliderSetting delay = add(new SliderSetting("UseDelay", 4, 0, 4, 1));
    private final Timer timer = new Timer();

    public InteractTweaks() {
        super("InteractTweaks", Category.Player);
        setChinese("交互调整");
        INSTANCE = this;
    }

    boolean swapped = false;
    int lastSlot = 0;

    @Override
    public void onLogin() {
        timer.reset();
    }

    @Override
    public void onUpdate() {
        if (multiTask()) {
            if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() != null && mc.options.attackKey.isPressed() && mc.player.getAttackCooldownProgress(0.5f) > 0.9f) {
                mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
        if (respawn.getValue() && mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();
            mc.setScreen(null);
            if (autoKit.getValue() && mc.player != null && timer.passedS(5)) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    mc.getNetworkHandler().sendChatCommand(command.getValue());
                });
            }
        }
        if (mc.itemUseCooldown <= 4 - delay.getValueInt()) {
            mc.itemUseCooldown = 0;
        }
        if (pickaxeSwitch.getValue()) {
            if (!(mc.player.getMainHandStack().getItem() instanceof PickaxeItem) && (!(mc.player.getMainHandStack().getItem() instanceof SwordItem) || allowSword.getValue()) && mc.player.getMainHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE && mc.player.getMainHandStack().getItem() != Items.GOLDEN_APPLE) {
                swapped = false;
                return;
            }
            int gapple = InventoryUtil.findItem(Items.ENCHANTED_GOLDEN_APPLE);
            if (gapple == -1) {
                gapple = InventoryUtil.findItem(Items.GOLDEN_APPLE);
            }
            if (gapple == -1) {
                if (swapped) {
                    InventoryUtil.switchToSlot(lastSlot);
                    swapped = false;
                }
                return;
            }
            if (mc.options.useKey.isPressed()) {
                if ((mc.player.getMainHandStack().getItem() instanceof PickaxeItem || (mc.player.getMainHandStack().getItem() instanceof SwordItem && allowSword.getValue())) && mc.player.getOffHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE && mc.player.getMainHandStack().getItem() != Items.GOLDEN_APPLE) {
                    lastSlot = mc.player.getInventory().selectedSlot;
                    InventoryUtil.switchToSlot(gapple);
                    swapped = true;
                }
            } else if (swapped) {
                InventoryUtil.switchToSlot(lastSlot);
                swapped = false;
            }
        }
    }

    public boolean isActive;

    @Override
    public void onDisable() {
        isActive = false;
    }

    public boolean reach() {
        return isOn() && reach.getValue();
    }

    public boolean noAbort() {
        return isOn() && noAbort.getValue() && !mc.options.useKey.isPressed();
    }

    public boolean noReset() {
        return isOn() && noReset.getValue();
    }

    public boolean noDelay() {
        return isOn() && noDelay.getValue();
    }

    public boolean multiTask() {
        return isOn() && multiTask.getValue();
    }

    public boolean noEntityTrace() {
        if (isOff() || !noEntityTrace.getValue()) return false;

        if (onlyPickaxe.getValue()) {
            return mc.player.getMainHandStack().getItem() instanceof PickaxeItem || mc.player.isUsingItem() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem);
        }
        return true;
    }

    public boolean ghostHand() {
        return isOn() && ghostHand.getValue() && !mc.options.useKey.isPressed() && !mc.options.sneakKey.isPressed();
    }
}
