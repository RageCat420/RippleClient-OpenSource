package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.LookAtEvent;
import dev.ripple.api.events.impl.UpdateWalkingPlayerEvent;
import dev.ripple.api.utils.combat.CombatUtil;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.Ripple;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.AntiCheat;
import dev.ripple.mod.modules.impl.exploit.Blink;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static dev.ripple.api.utils.world.BlockUtil.*;

public class AutoWeb extends Module {
    public static AutoWeb INSTANCE;
    public AutoWeb() {
        super("AutoWeb", Category.Combat);
        setChinese("蜘蛛网光环");
        INSTANCE = this;
    }

    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
    public final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500, () -> page.getValue() == Page.General));
    public final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 2, 1, 10, () -> page.getValue() == Page.General));
    public final SliderSetting predictTicks =
            add(new SliderSetting("PredictTicks", 2, 0.0, 50, 1, () -> page.getValue() == Page.General));
    private final BooleanSetting preferAnchor = add(new BooleanSetting("PreferAnchor", true, () -> page.getValue() == Page.General));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", true, () -> page.getValue() == Page.General));
    private final BooleanSetting onlyTick =
            add(new BooleanSetting("OnlyTick", false, () -> page.getValue() == Page.General));
    private final BooleanSetting feet =
            add(new BooleanSetting("Feet", true, () -> page.getValue() == Page.General));
    private final BooleanSetting face =
            add(new BooleanSetting("Face", true, () -> page.getValue() == Page.General));
    public final SliderSetting maxWebs =
            add(new SliderSetting("MaxWebs", 2, 1, 8, 1, () -> page.getValue() == Page.General));
    private final BooleanSetting down =
            add(new BooleanSetting("Down", true, () -> page.getValue() == Page.General));
    private final BooleanSetting inventorySwap =
            add(new BooleanSetting("InventorySwap", true, () -> page.getValue() == Page.General));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true, () -> page.getValue() == Page.General).setParent());
    private final BooleanSetting sameHand = add(new BooleanSetting("SameHand", true, () -> page.getValue() == Page.General && usingPause.isOpen()));

    public final SliderSetting offset =
            add(new SliderSetting("Offset", 0.25, 0.0, 0.3, 0.01, () -> page.getValue() == Page.General));
    public final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0, 0.1, () -> page.getValue() == Page.General));
    public final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 8.0, 0.0, 8.0, 0.1, () -> page.getValue() == Page.General));

    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true, () -> page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting yawStep =
            add(new BooleanSetting("YawStep", false, () -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting steps =
            add(new SliderSetting("Steps", 0.3, 0.1, 1.0, 0.01, () -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkFov =
            add(new BooleanSetting("OnlyLooking", true, () -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 30, 0, 50, () -> rotate.isOpen() && yawStep.getValue() && checkFov.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting priority = add(new SliderSetting("Priority", 10,0 ,100, () ->rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
    private final Timer timer = new Timer();
    public Vec3d directionVec = null;

    @Override
    public String getInfo() {
        if (pos.isEmpty()) return null;
        return "Working";
    }

    public static boolean force = false;
    public static boolean ignore = false;
    @EventHandler
    public void onRotate(LookAtEvent event) {
        if (rotate.getValue() && yawStep.getValue() && directionVec != null) {
            event.setTarget(directionVec, steps.getValueFloat(), priority.getValueFloat());
        }
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (!onlyTick.getValue()) {
            onUpdate();
        }
    }
    @Override
    public void onDisable() {
        force = false;
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (!onlyTick.getValue()) {
            onUpdate();
        }
    }

    int progress = 0;

    private final ArrayList<BlockPos> pos = new ArrayList<>();
    @Override
    public void onUpdate() {
        if (force) ignore = true;
        update();
        ignore = false;
    }

    private void update() {
        if (!timer.passedMs(placeDelay.getValueInt())) {
            return;
        }
        pos.clear();
        progress = 0;
        directionVec = null;
        if (preferAnchor.getValue() && AutoAnchor.INSTANCE.currentPos != null) {
            return;
        }
        if (getWebSlot() == -1) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
        if ((usingPause.getValue() && mc.player.isUsingItem() && !sameHand.getValue()) || (usingPause.getValue() && mc.player.isUsingItem() && sameHand.getValue() && mc.player.getActiveHand() == Hand.MAIN_HAND)) {
            return;
        }
        for (PlayerEntity player : CombatUtil.getEnemies(targetRange.getValue())) {
            Vec3d playerPos = predictTicks.getValue() > 0 ? CombatUtil.getEntityPosVec(player, predictTicks.getValueInt()) : player.getPos();
            int webs = 0;
            if (down.getValue()) {
                placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY() - 0.8, playerPos.getZ()));
            }
            List<BlockPos> list = new ArrayList<>();
            for (float x : new float[]{0, offset.getValueFloat(), -offset.getValueFloat()}) {
                for (float z : new float[]{0, offset.getValueFloat(), -offset.getValueFloat()}) {
                    for (float y : new float[]{0, 1, -1}) {
                        BlockPosX pos = new BlockPosX(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                        if (!list.contains(pos)) {
                            list.add(pos);
                            if (isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB && !Ripple.BREAK.isMining(pos)) {
                                webs++;
                            }
                        }
                    }
                }
            }
            if (webs >= maxWebs.getValueFloat() && !ignore) {
                continue;
            }
            boolean skip = false;
            if (feet.getValue()) {
                start:
                for (float x : new float[]{0, offset.getValueFloat(), -offset.getValueFloat()}) {
                    for (float z : new float[]{0, offset.getValueFloat(), -offset.getValueFloat()}) {
                        BlockPosX pos = new BlockPosX(playerPos.getX() + x, playerPos.getY(), playerPos.getZ() + z);
                        if (isTargetHere(pos, player)) {
                            if (placeWeb(pos)) {
                                webs++;
                                if (webs >= maxWebs.getValueFloat()) {
                                    skip = true;
                                    break start;
                                }
                            }
                        }
                    }
                }
            }
            if (skip) continue;
            if (face.getValue()) {
                start:
                for (float x : new float[]{0, offset.getValueFloat(), -offset.getValueFloat()}) {
                    for (float z : new float[]{0, offset.getValueFloat(), -offset.getValueFloat()}) {
                        BlockPosX pos = new BlockPosX(playerPos.getX() + x, playerPos.getY() + 1.1, playerPos.getZ() + z);
                        if (isTargetHere(pos, player)) {
                            if (placeWeb(pos)) {
                                webs++;
                                if (webs >= maxWebs.getValueFloat()) {
                                    break start;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean isTargetHere(BlockPos pos, PlayerEntity target) {
        return new Box(pos).intersects(target.getBoundingBox());
    }
    private boolean placeWeb(BlockPos pos) {
        if (this.pos.contains(pos)) return false;
        this.pos.add(pos);
        if (progress >= blocksPer.getValueInt()) return false;
        if (getWebSlot() == -1) {
            return false;
        }
        if (detectMining.getValue() && (Ripple.BREAK.isMining(pos))) return false;
        if (BlockUtil.getPlaceSide(pos, placeRange.getValue()) != null && (mc.world.isAir(pos) || ignore && getBlock(pos) == Blocks.COBWEB) && pos.getY() < 320) {
            int oldSlot = mc.player.getInventory().selectedSlot;
            int webSlot = getWebSlot();
            if (!placeBlock(pos, rotate.getValue(), webSlot)) return false;
            BlockUtil.placedPos.add(pos);
            progress++;
            if (inventorySwap.getValue()) {
                doSwap(webSlot);
                EntityUtil.syncInventory();
            } else {
                doSwap(oldSlot);
            }
            force = false;
            timer.reset();
            return true;
        }
        return false;
    }


    public boolean placeBlock(BlockPos pos, boolean rotate, int slot) {
        Direction side = getPlaceSide(pos);
        if (side == null) {
            if (airPlace()) {
                return clickBlock(pos, Direction.DOWN, rotate, slot);
            }
            return false;
        }
        return clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
    }

    public boolean clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
        Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (rotate) {
            if (!faceVector(directionVec)) return false;
        }
        doSwap(slot);
        EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.swingMode.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, id));
        if (rotate && !yawStep.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
            Ripple.ROTATION.snapBack();
        }
        return true;
    }

    private boolean faceVector(Vec3d directionVec) {
        if (!yawStep.getValue()) {
            Ripple.ROTATION.lookAt(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            if (Ripple.ROTATION.inFov(directionVec, fov.getValueFloat())) {
                return true;
            }
        }
        return !checkFov.getValue();
    }

    private void doSwap(int slot) {
        if (inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getWebSlot() {
        if (inventorySwap.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
        } else {
            return InventoryUtil.findBlock(Blocks.COBWEB);
        }
    }

    public enum Page {
        General,
        Rotate
    }
}