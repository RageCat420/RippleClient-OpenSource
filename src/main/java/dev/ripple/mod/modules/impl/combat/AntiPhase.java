package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.utils.combat.CombatUtil;
import dev.ripple.api.utils.entity.PauseUtil;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.AntiCheat;
import dev.ripple.mod.modules.settings.Placement;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class AntiPhase extends Module {
    public static AntiPhase INSTANCE;

    public AntiPhase() {
        super("AntiPhase", Category.Combat);
        setChinese("反卡墙");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        target = null;
    }

    public PlayerEntity target;
    private final SliderSetting targetRange = add(new SliderSetting("TargetRange", 6f, 0f, 12f));
    private final BooleanSetting crawl = add(new BooleanSetting("EnemyCrawling", false));
    private final SliderSetting range = add(new SliderSetting("PlaceRange", 5f, 0f, 6f));
    private final BooleanSetting self = add(new BooleanSetting("ProtectSelf", false));
    private final BooleanSetting packet = add(new BooleanSetting("PacketPlace", false));
    private final BooleanSetting pause = add(new BooleanSetting("PauseOnEat", true).setParent());
    private final BooleanSetting sameHand = add(new BooleanSetting("SameHand", true, pause::isOpen));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting ladder = add(new BooleanSetting("Ladder", true));
    private final BooleanSetting frame = add(new BooleanSetting("Frame", true));
    private final BooleanSetting sca = add(new BooleanSetting("Scaffolding", true));

    private void doSwap(int slot, int selectSlot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, selectSlot);
            EntityUtil.syncInventory();
        } else if (slot < 9) {
            InventoryUtil.switchToSlot(slot);
        }
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        target = CombatUtil.getClosestEnemy(targetRange.getValueFloat());
        if (target == null) return;
        if (target.isCrawling() && !crawl.getValue()) return;
        if (MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(target.getBlockPos().toCenterPos())) > range.getValueFloat()) {
            return;
        }
        if (self.getValue() && target.getBlockPos().toCenterPos() == mc.player.getBlockPos().toCenterPos()) return;
        if (pause.getValue() && PauseUtil.checkPause(sameHand.getValue())) return;
        if (frame.getValue()) placeF();
        if (sca.getValue()) placeS();
        if (ladder.getValue()) placeL();
    }

    public void placeL() {
        int temp = InventoryUtil.findItemInventorySlot(Items.LADDER);
        int prev = mc.player.getInventory().selectedSlot;
        if (temp != -1 && target != null) {
            Block block = mc.world.getBlockState(target.getBlockPos()).getBlock();
            BlockPos pos = target.getBlockPos();
            if (!checkOffsets(pos)) return;
            boolean canPlace = mc.world.canPlace(Blocks.LADDER.getDefaultState(), target.getBlockPos(), ShapeContext.absent()) && (block == Blocks.AIR || block == null);
            if (canPlace) {
                doSwap(temp, prev);
                BlockUtil.placeBlock(target.getBlockPos(), true, packet.getValue());
                doSwap(temp, prev);
            }
        }
    }

    private boolean checkOffsets(BlockPos pos) {
        for (Direction i : Direction.values()) {
            if (i == Direction.DOWN || i == Direction.UP) continue;
            if (mc.world.isAir(pos.offset(i))) continue;
            if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite()) && AntiCheat.INSTANCE.placement.is(Placement.Strict)) continue;
            if (!EntityUtil.canSee(pos.offset(i), i.getOpposite()) && AntiCheat.INSTANCE.placement.is(Placement.Legit)) continue;
            return true;
        }
        return false;
    }

    public void placeS() {
        int temp = InventoryUtil.findItemInventorySlot(Items.SCAFFOLDING);
        int prev = mc.player.getInventory().selectedSlot;
        if (temp != -1 && target != null) {
            boolean canPlace = mc.world.canPlace(Blocks.SCAFFOLDING.getDefaultState(), target.getBlockPos(), ShapeContext.absent());
            if (canPlace) {
                doSwap(temp, prev);
                BlockUtil.placeBlock(target.getBlockPos(), true, packet.getValue());
                doSwap(temp, prev);
            }
        }
    }

    public void placeF() {
        int temp = InventoryUtil.findItemInventorySlot(Items.ITEM_FRAME);
        int temp2 = InventoryUtil.findItemInventorySlot(Items.GLOW_ITEM_FRAME);
        int prev = mc.player.getInventory().selectedSlot;
        if (temp != -1 && target != null) {
            Block block = mc.world.getBlockState(target.getBlockPos()).getBlock();
            boolean canPlace = (mc.world.isAir(target.getBlockPos()) || block == Blocks.SCAFFOLDING) && !checkFrame().contains(target.getBlockPos());
            if (canPlace) {
                doSwap(temp, prev);
                BlockUtil.placeBlockGround(target.getBlockPos(), true, packet.getValue());
                doSwap(temp, prev);
            }
        } else if (temp2 != -1 && target != null) {
            Block block = mc.world.getBlockState(target.getBlockPos()).getBlock();
            boolean canPlace = (mc.world.isAir(target.getBlockPos()) || block == Blocks.SCAFFOLDING) && !checkFrame().contains(target.getBlockPos());
            if (canPlace) {
                doSwap(temp2, prev);
                BlockUtil.placeBlockGround(target.getBlockPos(), true, packet.getValue());
                doSwap(temp2, prev);
            }
        }
    }

    private List<BlockPos> checkFrame() {
        List<BlockPos> temp = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if ((entity instanceof ItemFrameEntity || entity instanceof GlowItemFrameEntity) && entity.getMovementDirection() == Direction.UP) {
                temp.add(entity.getBlockPos());
            }
        }
        return temp;
    }
}