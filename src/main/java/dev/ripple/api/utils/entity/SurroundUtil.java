package dev.ripple.api.utils.entity;

import dev.ripple.api.utils.world.BlockPosX;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.ripple.api.utils.Wrapper.mc;

public class SurroundUtil {
    public static boolean checkFullGodSurround(Vec3d vec) {
        List<Block> temp = new ArrayList<>();
        BlockPos pos = new BlockPosX(vec);
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN) continue;
            if (!mc.world.isAir(pos.offset(i))) {
                temp.add(mc.world.getBlockState(pos.offset(i)).getBlock());
            }
        }
        for (Block block : temp) {
            if (!godBlocks.contains(block)) return false;
        }
        return true;
    }

    public static boolean checkAirSurround(Vec3d vec) {
        BlockPos pos = new BlockPosX(vec);
        for (Direction i : Direction.values()) {
            if (i == Direction.UP || i == Direction.DOWN) continue;
            if (mc.world.isAir(pos.offset(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkGodBurrow(Vec3d pos) {
        double[] xzOffset = new double[]{0.3, -0.3};
        for (double offset : xzOffset) {
            BlockPos offsetPos = new BlockPosX(pos.getX() + offset, pos.getY(), pos.getZ() + offset);
            if (!godBlocks.contains(mc.world.getBlockState(offsetPos).getBlock()) || mc.world.isAir(offsetPos)) {
                return false;
            }
        }
        for (double o : xzOffset) {
            for (double offset : xzOffset) {
                BlockPos offsetPos = new BlockPosX(pos.getX() + offset, pos.getY(), pos.getZ() + o);
                if (!godBlocks.contains(mc.world.getBlockState(offsetPos).getBlock()) || mc.world.isAir(offsetPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkAirBurrow(Vec3d pos) {
        double[] xzOffset = new double[]{0.3, -0.3};
        for (double offset : xzOffset) {
            BlockPos offsetPos = new BlockPosX(pos.getX() + offset, pos.getY(), pos.getZ() + offset);
            if (mc.world.isAir(offsetPos)) {
                return true;
            }
        }
        for (double o : xzOffset) {
            for (double offset : xzOffset) {
                BlockPos offsetPos = new BlockPosX(pos.getX() + offset, pos.getY(), pos.getZ() + o);
                if (mc.world.isAir(offsetPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkGodBurrowForSur(Vec3d pos) {
        double[] xzOffset = new double[]{0.3, -0.3};
        for (double offset : xzOffset) {
            BlockPos offsetPos = new BlockPosX(pos.getX() + offset, pos.getY(), pos.getZ() + offset);
            if (!godBlocks2.contains(mc.world.getBlockState(offsetPos).getBlock()) || mc.world.isAir(offsetPos)) {
                return false;
            }
        }
        for (double o : xzOffset) {
            for (double offset : xzOffset) {
                BlockPos offsetPos = new BlockPosX(pos.getX() + offset, pos.getY(), pos.getZ() + o);
                if (!godBlocks2.contains(mc.world.getBlockState(offsetPos).getBlock()) || mc.world.isAir(offsetPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static final List<Block> godBlocks = Arrays.asList(Blocks.COMMAND_BLOCK, Blocks.BEDROCK, Blocks.BARRIER, Blocks.END_PORTAL_FRAME);
    public static final List<Block> godBlocks2 = Arrays.asList(Blocks.COMMAND_BLOCK, Blocks.BEDROCK, Blocks.BARRIER, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME);

}
