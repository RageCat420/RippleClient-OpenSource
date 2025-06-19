package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.utils.combat.CombatUtil;
import dev.ripple.api.utils.entity.PauseUtil;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class FishTrap extends Module {
    public static FishTrap INSTANCE;

    public FishTrap() {
        super("FishTrap", Category.Combat);
        setChinese("爬行困住");
        INSTANCE = this;
    }

    private enum Target {
        AutoCrystal, Range, Either
    }

    private final EnumSetting<Target> targetMode = add(new EnumSetting<>("TargetMode", Target.Either));
    private final SliderSetting targetRange = add(new SliderSetting("TargetRange", 6f, 0f, 12f, () -> targetMode.getValue() != Target.AutoCrystal));
    private final BooleanSetting self = add(new BooleanSetting("ProtectSelf", true));
    private final SliderSetting range = add(new SliderSetting("PlaceRange", 5f, 0f, 6f));
    private final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packet = add(new BooleanSetting("PacketPlace", false));
    public final BooleanSetting pause = add(new BooleanSetting("PauseOnEat", true).setParent());
    public final BooleanSetting sameHand = add(new BooleanSetting("SameHand", true, pause::isOpen));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));

    double[] xzOffset = new double[]{0, 0.3, -0.3};

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (pause.getValue() && PauseUtil.checkPause(sameHand.getValue())) return;
        PlayerEntity target = targetMode.getValue() == Target.AutoCrystal ? AutoCrystal.INSTANCE.displayTarget
                : targetMode.getValue() == Target.Range ? CombatUtil.getClosestEnemy(targetRange.getValueFloat())
                : AutoCrystal.INSTANCE.displayTarget == null ? CombatUtil.getClosestEnemy(targetRange.getValueFloat())
                : AutoCrystal.INSTANCE.displayTarget;
        if (target != null && target.isCrawling()) {
            for (double offset : xzOffset) {
                for (double offset2 : xzOffset) {
                    BlockPos pos = new BlockPosX(target.getX() + offset, target.getY() + 1.2, target.getZ() + offset2);
                    if (checkSelf().contains(pos.toCenterPos()) && self.getValue()) continue;
                    doPlace(target, pos);
                }
            }
        }
    }

    private List<Vec3d> checkSelf() {
        List<Vec3d> temp = new ArrayList<>();
        for (double offset : xzOffset) {
            for (double offset2 : xzOffset) {
                Vec3d pos = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.2, mc.player.getZ() + offset2).toCenterPos();
                temp.add(pos);
            }
        }
        return temp;
    }

    private void doSwap(int slot, int selectSlot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, selectSlot);
            EntityUtil.syncInventory();
        } else if (slot < 9) {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void doPlace(PlayerEntity target, BlockPos pos) {
        int temp = InventoryUtil.findItemInventorySlot(Items.OBSIDIAN);
        int prev = mc.player.getInventory().selectedSlot;
        if (temp != -1 && target != null) {
            //boolean canPlace = mc.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent());
            if (BlockUtil.canPlace(pos, range.getValueFloat(), false)) {
                doSwap(temp, prev);
                BlockUtil.placeBlock(pos, rotate.getValue(), packet.getValue());
                doSwap(temp, prev);
            }
        }
    }
}