package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.utils.combat.CombatUtil;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.SurroundUtil;
import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.player.SpeedMine;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.ripple.api.utils.world.BlockUtil.getBlock;

public class AutoMine extends Module {
    public static AutoMine INSTANCE;
    private final BooleanSetting burrow = add(new BooleanSetting("Burrow", true));
    private final BooleanSetting face = add(new BooleanSetting("Face", false));
    private final BooleanSetting down = add(new BooleanSetting("Down", true).setParent());
    private final BooleanSetting onlyGod = add(new BooleanSetting("OnlyGodBlock", true, down::isOpen));
    private final BooleanSetting surround = add(new BooleanSetting("Surround", true).setParent());
    private final BooleanSetting checkBurrow = add(new BooleanSetting("CheckBurrow", true, surround::isOpen));
    private final BooleanSetting lowVersion = add(new BooleanSetting("1.12", false, surround::isOpen));
    private final BooleanSetting ignore = add(new BooleanSetting("BreakAntiPhase", false));
    public final SliderSetting targetRange = add(new SliderSetting("TargetRange", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
    public final SliderSetting range = add(new SliderSetting("Range", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));

    private int mineNum = 0;

    public AutoMine() {
        super("AutoMine", Category.Combat);
        setChinese("自动挖掘");
        INSTANCE = this;
    }

    @Override
    public void onLogin() {
        mineNum = 0;
    }

    @Override
    public void onEnable() {
        mineNum = 0;
    }

    @Override
    public void onUpdate() {
        if (AntiCrawl.INSTANCE.work) return;
        PlayerEntity player = CombatUtil.getClosestEnemy(targetRange.getValue());
        if (player == null) return;
        mineNum = 0;
        doBreak(player);
    }

    private void doBreak(PlayerEntity player) {
        BlockPos pos = EntityUtil.getEntityPos(player, true);
        {
            double[] yOffset = new double[]{-0.8, 0.5, 1.1};
            double[] xzOffset = new double[]{0.3, -0.3};
            /*
            for (PlayerEntity entity : CombatUtil.getEnemies(targetRange.getValue())) {
                for (double y : yOffset) {
                    for (double x : xzOffset) {
                        for (double z : xzOffset) {
                            BlockPos offsetPos = new BlockPosX(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
                            if (canBreak(offsetPos) && offsetPos.equals(SpeedMine.getBreakPos())) {
                                return;
                            }
                        }
                    }
                }
            }
             */
            List<Float> yList = new ArrayList<>();
            if (down.getValue()) {
                if (onlyGod.getValue() && ((SurroundUtil.checkFullGodSurround(pos.toCenterPos()) && !SurroundUtil.checkAirSurround(pos.toCenterPos())) || (SurroundUtil.checkGodBurrow(pos.toCenterPos()) && !SurroundUtil.checkAirBurrow(pos.toCenterPos())))) {
                    yList.add(-0.8f);
                } else if (!onlyGod.getValue()) {
                    yList.add(-0.8f);
                }
            }
            if (burrow.getValue()) {
                yList.add(0.5f);
            }
            if (face.getValue()) {
                yList.add(1.1f);
            }
            for (double y : yList) {
                for (double offset : xzOffset) {
                    BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
                    if (AntiPhase.INSTANCE.isOn() && AntiPhase.INSTANCE.target != null) {
                        if (new Box(pos).intersects(new Box(AntiPhase.INSTANCE.target.getBlockPos())) && mc.world.getBlockState(pos).getBlock() == Blocks.LADDER && mc.world.getBlockState(pos).getBlock() == Blocks.SCAFFOLDING && !ignore.getValue())
                            continue;
                    }
                    if (canBreak(offsetPos)) {
                        mine(offsetPos);
                    }
                }
            }
            for (double y : yList) {
                for (double offset : xzOffset) {
                    for (double offset2 : xzOffset) {
                        BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                        if (AntiPhase.INSTANCE.isOn() && AntiPhase.INSTANCE.target != null) {
                            if (new Box(pos).intersects(new Box(AntiPhase.INSTANCE.target.getBlockPos())) && mc.world.getBlockState(pos).getBlock() == Blocks.LADDER && mc.world.getBlockState(pos).getBlock() == Blocks.SCAFFOLDING && !ignore.getValue())
                                continue;
                        }
                        if (canBreak(offsetPos)) {
                            mine(offsetPos);
                        }
                    }
                }
            }
        }
        if (surround.getValue() && (!checkBurrow.getValue() || !SurroundUtil.checkGodBurrowForSur(pos.toCenterPos()))) {
            if (!lowVersion.getValue()) {
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                        continue;
                    }
                    //if ((mc.world.isAir(pos.offset(i)) || pos.offset(i).equals(SpeedMine.getBreakPos())) && canPlaceCrystal(pos.offset(i), false)) {
                        //return;
                    //}
                }
                ArrayList<BlockPos> list = new ArrayList<>();
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                        continue;
                    }
                    if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), true)) {
                        list.add(pos.offset(i));
                    }
                }
                if (!list.isEmpty()) {
                    //System.out.println("found");
                    mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
                } else {
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP || i == Direction.DOWN) continue;
                        if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                            continue;
                        }
                        if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), false)) {
                            list.add(pos.offset(i));
                        }
                    }
                    if (!list.isEmpty()) {
                        //System.out.println("found");
                        mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
                    }
                }

            } else {

                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (mc.player.getEyePos().distanceTo(pos.offset(i).toCenterPos()) > range.getValue()) {
                        continue;
                    }
                    //if ((mc.world.isAir(pos.offset(i)) && mc.world.isAir(pos.offset(i).up())) && canPlaceCrystal(pos.offset(i), false)) {
                        //return;
                    //}
                }

                ArrayList<BlockPos> list = new ArrayList<>();
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
                        continue;
                    }
                    if (canCrystal(pos.offset(i))) {
                        list.add(pos.offset(i));
                    }
                }

                int max = 0;
                BlockPos minePos = null;
                for (BlockPos cPos : list) {
                    if (getAir(cPos) >= max) {
                        max = getAir(cPos);
                        minePos = cPos;
                    }
                }
                if (minePos != null) {
                    doMine(minePos);
                }
            }
        }
        if (SpeedMine.getBreakPos() == null) {
            if (burrow.getValue()) {
                double[] yOffset;
                double[] xzOffset = new double[]{0, 0.3, -0.3};

                yOffset = new double[]{0.5, 1.1};
                for (double y : yOffset) {
                    for (double offset : xzOffset) {
                        BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
                        if (isObsidian(offsetPos)) {
                            mine(offsetPos);
                        }
                    }
                }
                for (double y : yOffset) {
                    for (double offset : xzOffset) {
                        for (double offset2 : xzOffset) {
                            BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                            if (isObsidian(offsetPos)) {
                                mine(offsetPos);
                            }
                        }
                    }
                }
            }
        }
    }

    private int getMaxNum() {
        return SpeedMine.INSTANCE.isOn() ? SpeedMine.INSTANCE.doubleBreak.getValue() ? 2 : 1 : 1;
    }

    private void doMine(BlockPos pos) {
        if (canBreak(pos)) {
            mine(pos);
        } else if (canBreak(pos.up())) {
            mine(pos.up());
        }
    }

    private void mine(BlockPos pos) {
        if (mineNum >= getMaxNum()) return;
        SpeedMine.INSTANCE.mine(pos);
        mineNum++;
    }

    private boolean canCrystal(BlockPos pos) {
        if (SpeedMine.godBlocks.contains(getBlock(pos)) || getBlock(pos) instanceof BedBlock || getBlock(pos) instanceof CobwebBlock || !canPlaceCrystal(pos, true) || BlockUtil.getClickSideStrict(pos) == null) {
            return false;
        }
        return !SpeedMine.godBlocks.contains(getBlock(pos.up())) && !(getBlock(pos.up()) instanceof BedBlock) && !(getBlock(pos.up()) instanceof CobwebBlock) && BlockUtil.getClickSideStrict(pos.up()) != null;
    }

    private int getAir(BlockPos pos) {
        int value = 0;
        if (!canBreak(pos)) {
            value++;
        }
        if (!canBreak(pos.up())) {
            value++;
        }

        return value;
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean block) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN || !block) && !BlockUtil.hasEntityBlockCrystal(boost, true, true) && !BlockUtil.hasEntityBlockCrystal(boost.up(), true, true) && (!lowVersion.getValue() || mc.world.isAir(boost.up()));
    }

    private boolean isObsidian(BlockPos pos) {
        return mc.player.getEyePos().distanceTo(pos.toCenterPos()) <= range.getValue() && (getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST || getBlock(pos) == Blocks.NETHERITE_BLOCK || getBlock(pos) == Blocks.RESPAWN_ANCHOR) && BlockUtil.getClickSideStrict(pos) != null;
    }

    private boolean canBreak(BlockPos pos) {
        return isObsidian(pos) && (BlockUtil.getClickSideStrict(pos) != null || SpeedMine.getBreakPos().equals(pos)) && (!pos.equals(SpeedMine.secondPos) || !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem));
    }
}