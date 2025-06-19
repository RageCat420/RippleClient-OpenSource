package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.MovementUtil;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.movement.ElytraFly;
import dev.ripple.mod.modules.impl.movement.FakeFly;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;

public class AutoArmor extends Module {
    private final EnumSetting<EnchantPriority> head = add(new EnumSetting<>("Head", EnchantPriority.Protection));
    private final EnumSetting<EnchantPriority> body = add(new EnumSetting<>("Body", EnchantPriority.Blast));
    private final EnumSetting<EnchantPriority> legs = add(new EnumSetting<>("Legs", EnchantPriority.Blast));
    private final EnumSetting<EnchantPriority> feet = add(new EnumSetting<>("Feet", EnchantPriority.Protection));
    private final BooleanSetting noMove = add(new BooleanSetting("NoMove", false));
    private final SliderSetting delay = add(new SliderSetting("Delay", 3, 0, 10, 1));
    public final BooleanSetting autoElytra = add(new BooleanSetting("AutoElytra", true));
    private final BooleanSetting snowBug = add(new BooleanSetting("SnowBug", true));
    private final BooleanSetting ignoreCurse = add(new BooleanSetting("IgnoreBinding", true));
    public static AutoArmor INSTANCE;

    public AutoArmor() {
        super("AutoArmor", Category.Combat);
        setChinese("自动穿甲");
        INSTANCE = this;
    }

    private enum EnchantPriority {
        Protection, Blast
    }

    private int tickDelay = 0;

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof UIScreen)) {
            return;
        }

        if (mc.player.playerScreenHandler != mc.player.currentScreenHandler) return;

        if (MovementUtil.isMoving() && noMove.getValue()) return;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        tickDelay = delay.getValueInt();

        Map<EquipmentSlot, int[]> armorMap = new HashMap<>(4);
        armorMap.put(EquipmentSlot.FEET, new int[]{36, getProtection(mc.player.getInventory().getStack(36)), -1, -1});
        armorMap.put(EquipmentSlot.LEGS, new int[]{37, getProtection(mc.player.getInventory().getStack(37)), -1, -1});
        armorMap.put(EquipmentSlot.CHEST, new int[]{38, getProtection(mc.player.getInventory().getStack(38)), -1, -1});
        armorMap.put(EquipmentSlot.HEAD, new int[]{39, getProtection(mc.player.getInventory().getStack(39)), -1, -1});
        for (int s = 0; s < 36; s++) {
            if (!(mc.player.getInventory().getStack(s).getItem() instanceof ArmorItem) && mc.player.getInventory().getStack(s).getItem() != Items.ELYTRA)
                continue;
            int protection = getProtection(mc.player.getInventory().getStack(s));
            EquipmentSlot slot = (mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem ? EquipmentSlot.CHEST : ((ArmorItem) mc.player.getInventory().getStack(s).getItem()).getSlotType());
            for (Map.Entry<EquipmentSlot, int[]> e : armorMap.entrySet()) {
                if (e.getKey() == EquipmentSlot.FEET) {
                    if (mc.player.hurtTime > 1 && snowBug.getValue()) {
                        if (!mc.player.getInventory().getStack(36).isEmpty() && mc.player.getInventory().getStack(36).getItem() == Items.LEATHER_BOOTS) {
                            continue;
                        }
                        if (!mc.player.getInventory().getStack(s).isEmpty() && mc.player.getInventory().getStack(s).getItem() == Items.LEATHER_BOOTS) {
                            e.getValue()[2] = s;
                            continue;
                        }
                    }
                }
                if (autoElytra.getValue() && (ElytraFly.INSTANCE.isOn()
                        || FakeFly.INSTANCE.isOn()) && e.getKey() == EquipmentSlot.CHEST) {
                    if (FakeFly.INSTANCE.isOn() && FakeFly.INSTANCE.armor.getValue()) {
                        e.getValue()[2] = -1;
                    } else {
                        if (!mc.player.getInventory().getStack(38).isEmpty() && mc.player.getInventory().getStack(38).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(38))) {
                            continue;
                        }
                        if (e.getValue()[2] != -1 && !mc.player.getInventory().getStack(e.getValue()[2]).isEmpty() && mc.player.getInventory().getStack(e.getValue()[2]).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(e.getValue()[2]))) {
                            continue;
                        }
                        if (!mc.player.getInventory().getStack(s).isEmpty() && mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(s))) {
                            e.getValue()[2] = s;
                        }
                        continue;
                    }
                }
                if (protection > 0) {
                    if (e.getKey() == slot) {
                        if (protection > e.getValue()[1] && protection > e.getValue()[3]) {
                            e.getValue()[2] = s;
                            e.getValue()[3] = protection;
                        }
                    }
                }
            }
        }

        for (Map.Entry<EquipmentSlot, int[]> equipmentSlotEntry : armorMap.entrySet()) {
            if (equipmentSlotEntry.getValue()[2] != -1) {
                if (equipmentSlotEntry.getValue()[1] == -1 && equipmentSlotEntry.getValue()[2] < 9) {
/*					if (equipmentSlotEntry.getValue()[2] != mc.player.getInventory().selectedSlot) {
						mc.player.getInventory().selectedSlot = equipmentSlotEntry.getValue()[2];
						mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(equipmentSlotEntry.getValue()[2]));
					}*/
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36 + equipmentSlotEntry.getValue()[2], 1, SlotActionType.QUICK_MOVE, mc.player);
                    EntityUtil.syncInventory();
                } else if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
                    int armorSlot = (equipmentSlotEntry.getValue()[0] - 34) + (39 - equipmentSlotEntry.getValue()[0]) * 2;
                    int newArmorSlot = equipmentSlotEntry.getValue()[2] < 9 ? 36 + equipmentSlotEntry.getValue()[2] : equipmentSlotEntry.getValue()[2];
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, mc.player);
                    if (equipmentSlotEntry.getValue()[1] != -1)
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, mc.player);
                    EntityUtil.syncInventory();
                }
                return;
            }
        }
    }

    private int getProtection(ItemStack is) {
        if (is.getItem() instanceof ArmorItem || is.getItem() == Items.ELYTRA) {
            int prot = 0;
            EquipmentSlot slot = is.getItem() instanceof ArmorItem ai ? ai.getSlotType() : EquipmentSlot.BODY;
            if (is.getItem() instanceof ElytraItem) {
                if (!ElytraItem.isUsable(is)) return 0;
                prot = 1;
            }
            int blastMultiplier = 1;
            int protectionMultiplier = 1;

            switch (slot) {
                case HEAD -> {
                    if (head.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
                case BODY -> {
                    if (body.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
                case LEGS -> {
                    if (legs.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
                case FEET -> {
                    if (feet.is(EnchantPriority.Protection)) protectionMultiplier *= 2;
                    else blastMultiplier *= 2;
                }
            }

            if (is.hasEnchantments()) {
                ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(is);

                if (enchants.getEnchantments().contains(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get()))
                    prot += enchants.getLevel(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get()) * protectionMultiplier;

                if (enchants.getEnchantments().contains(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get()))
                    prot += enchants.getLevel(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get()) * blastMultiplier;

                if (enchants.getEnchantments().contains(mc.world.getRegistryManager().get(Enchantments.BINDING_CURSE.getRegistryRef()).getEntry(Enchantments.BINDING_CURSE).get()) && ignoreCurse.getValue())
                    prot = -999;
            }

            return (is.getItem() instanceof ArmorItem armorItem ? (armorItem.getProtection() + (int) Math.ceil(armorItem.getToughness())) * 10 : 0) + prot;
        } else if (!is.isEmpty()) {
            return 0;
        }
        return -1;
    }
}