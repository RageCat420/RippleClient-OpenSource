package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class AutoLog extends Module {
    public static AutoLog INSTANCE; //日志 + y log
    private final SliderSetting hp = add(new SliderSetting("Hp", 6f, 0f, 36f, 0.1));
    private final SliderSetting totems = add(new SliderSetting("Totems", 3, 0, 36));
    private final BooleanSetting absorption = add(new BooleanSetting("CalcAbsorption", false));
    private final BooleanSetting coordinate = add(new BooleanSetting("Coordinate", true));
    private final BooleanSetting dimension = add(new BooleanSetting("Dimension", true));
    private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable", true));

    public AutoLog() {
        super("AutoLog", Category.Combat);
        setChinese("自动下线");
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if ((mc.player.getHealth() + (absorption.getValue() ? mc.player.getAbsorptionAmount() : 0)) <= hp.getValue() && InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING) <= totems.getValueInt()) {
            mc.getNetworkHandler().getConnection().disconnect(Text.of("[AutoLog]"
                    + "\nHp: " + Math.round(mc.player.getHealth() + (absorption.getValue() ? mc.player.getAbsorptionAmount() : 0))
                    + " Totems: " + InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING)
                    + (coordinate.getValue() ? "\nx: " + mc.player.getBlockX() : "")
                    + (coordinate.getValue() ? " y: " + mc.player.getBlockY() : "")
                    + (coordinate.getValue() ? " z: " + mc.player.getBlockZ() : "")
                    + (dimension.getValue() ? "\nDimension: " + getDimension(mc.world) : "")));
            if (autoDisable.getValue()) disable();
        }
    }

    public String getDimension(World world) {
        String str = world.getRegistryKey().getValue().toString();
        if (str.contains("overworld")) {
            return "The Overworld";
        } else if (str.contains("nether")) {
            return "The Nether";
        } else if (str.contains("end")) {
            return "The End";
        } else {
            return "Unknown";
        }
    }
}