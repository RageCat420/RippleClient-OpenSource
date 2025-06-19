package dev.ripple.mod.modules.impl.render;

import dev.ripple.Ripple;
import dev.ripple.api.utils.render.Render3DUtil;
import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.ArrayList;

public class PhaseESP extends Module {
    public static PhaseESP INSTANCE;
    private final SliderSetting accuracy = add(new SliderSetting("CollideAccuracy", 0.001, 0.001, 1, 0.0001));
    private final BooleanSetting collide = add(new BooleanSetting("OnlyMoveKey", false));
    private final BooleanSetting crawl = add(new BooleanSetting("WhenCrawling", false));
    private final BooleanSetting insideBlock = add(new BooleanSetting("InsideBlock", false).setParent());
    private final BooleanSetting selfPos = add(new BooleanSetting("SelfPos", true, insideBlock::isOpen));
    private final BooleanSetting fix = add(new BooleanSetting("YAxisFix", true));
    private final ColorSetting hard = add(new ColorSetting("HardLine", new Color(100, 255, 0, 125))).injectBoolean(true);
    private final ColorSetting hard2 = add(new ColorSetting("HardFill", new Color(100, 255, 0, 75))).injectBoolean(true);
    private final ColorSetting medium = add(new ColorSetting("MediumLine", new Color(0, 175, 255, 125))).injectBoolean(true);
    private final ColorSetting medium2 = add(new ColorSetting("MediumFill", new Color(0, 175, 255, 75))).injectBoolean(true);
    private final ColorSetting weak = add(new ColorSetting("WeakLine", new Color(255, 200, 0, 125))).injectBoolean(true);
    private final ColorSetting weak2 = add(new ColorSetting("WeakFill", new Color(255, 200, 0, 75))).injectBoolean(true);
    private final ColorSetting air = add(new ColorSetting("AirLine", new Color(255, 0, 0, 125))).injectBoolean(true);
    private final ColorSetting air2 = add(new ColorSetting("AirFill", new Color(255, 0, 0, 75))).injectBoolean(true);

    public PhaseESP() {
        super("PhaseESP", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        double[] xzOffset = new double[]{0.3 + accuracy.getValueFloat() / 10, -(0.3 + accuracy.getValueFloat() / 10)};
        BlockPos self = new BlockPosX(mc.player.getPos(), fix.getValue());
        Box selfBox = new Box(self);
        ArrayList<BlockPos> p = new ArrayList<>();
        for (double offset : xzOffset) {
            for (double offset2 : xzOffset) {
                BlockPos pos = new BlockPosX(mc.player.getX() + offset, mc.player.getY(), mc.player.getZ() + offset2, fix.getValue());
                Block block = getBlock(pos);
                BlockPos down = pos.down();
                Block dBlock = getBlock(down);
                if (collide.getValue() && !mc.player.horizontalCollision) return;
                if (!crawl.getValue() && mc.player.isCrawling()) return;
                if (!insideBlock.getValue() && Ripple.PLAYER.insideBlock) return;
                if (block != Blocks.OBSIDIAN && block != Blocks.CRYING_OBSIDIAN && block != Blocks.ANCIENT_DEBRIS && block != Blocks.NETHERITE_BLOCK && block != Blocks.ENDER_CHEST && block != Blocks.REINFORCED_DEEPSLATE && block != Blocks.BEDROCK && block != Blocks.BARRIER && block != Blocks.RESPAWN_ANCHOR)
                    continue;
                Box box = new Box(pos);
                if (selfBox.intersects(box) && !Ripple.PLAYER.insideBlock) continue;
                if (mc.player.getBoundingBox().intersects(box) && !selfPos.getValue()) continue;
                if (p.contains(pos)) continue;
                p.add(pos);
                if (dBlock == Blocks.BEDROCK || dBlock == Blocks.BARRIER || dBlock.getHardness() <= -1) {
                    if (hard.booleanValue)
                        render(stack, pos, hard.getValue(), hard2.getValue(), hard.booleanValue, hard2.booleanValue);
                } else if (dBlock == Blocks.OBSIDIAN || dBlock == Blocks.CRYING_OBSIDIAN || dBlock == Blocks.ANCIENT_DEBRIS || dBlock == Blocks.NETHERITE_BLOCK || dBlock == Blocks.ENDER_CHEST || dBlock == Blocks.REINFORCED_DEEPSLATE) {
                    if (medium.booleanValue)
                        render(stack, pos, medium.getValue(), medium2.getValue(), medium.booleanValue, medium2.booleanValue);
                } else if (mc.world.isAir(down) || !mc.world.getBlockState(down).isSolidBlock(mc.world, down)) {
                    if (air.booleanValue)
                        render(stack, pos, air.getValue(), air2.getValue(), air.booleanValue, air2.booleanValue);
                } else {
                    if (weak.booleanValue)
                        render(stack, pos, weak.getValue(), weak2.getValue(), weak.booleanValue, weak2.booleanValue);
                }
            }
        }
    }

    public void render(MatrixStack stack, BlockPos pos, Color line, Color fill, boolean bl, boolean bf) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.01, pos.getZ() + 1);
        if (bl) Render3DUtil.drawBox(stack, box, line);
        if (bf) Render3DUtil.drawFill(stack, box, fill);
    }

    public Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }
}