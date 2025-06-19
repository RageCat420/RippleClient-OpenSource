package dev.ripple.mod.modules.impl.movement;

import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.utils.entity.MovementUtil;
import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.mod.modules.Module;
import net.minecraft.block.Blocks;


public class AntiVoid extends Module {

    private final SliderSetting voidHeight =
            add(new SliderSetting("VoidHeight", -64, -64, 319, 1));
    private final SliderSetting height =
            add(new SliderSetting("Height", 100, -40, 256, 1));

    public AntiVoid() {
        super("AntiVoid", "Allows you to fly over void blocks", Category.Movement);
        setChinese("反虚空");
    }

    @Override
    public void onUpdate() {
        boolean isVoid = true;

        for (int i = (int) mc.player.getY(); i > voidHeight.getValueInt() - 1; --i) {
            if (!mc.world.getBlockState(new BlockPosX(mc.player.getX(), i, mc.player.getZ())).isAir() && mc.world.getBlockState(new BlockPosX(mc.player.getX(), i, mc.player.getZ())).getBlock() != Blocks.VOID_AIR) {
                isVoid = false;
                break;
            }
        }
        if (mc.player.getY() < height.getValue() + voidHeight.getValue() && isVoid) {
            MovementUtil.setMotionY(0);
        }
    }
}
