package dev.ripple.mod.modules.impl.client;

import dev.ripple.Ripple;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.Placement;
import dev.ripple.mod.modules.settings.SwingSide;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

public class AntiCheat extends Module {
    public static AntiCheat INSTANCE;
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));

    public final BooleanSetting multiPlace = add(new BooleanSetting("MultiPlace", true, () -> page.is(Page.General)));
    public final BooleanSetting packetPlace = add(new BooleanSetting("PacketPlace", true, () -> page.is(Page.General)));
    public final BooleanSetting attackRotate = add(new BooleanSetting("AttackRotation", false, () -> page.is(Page.General)));
    public final BooleanSetting invSwapBypass = add(new BooleanSetting("PickSwap", false, () -> page.is(Page.General)));
    public final SliderSetting boxSize = add(new SliderSetting("HitBoxSize", 0.6, 0, 1, 0.01, () -> page.is(Page.General)));
    public final SliderSetting attackDelay = add(new SliderSetting("BreakDelay", 0.2, 0, 1, 0.01, () -> page.is(Page.General)).setSuffix("s"));
    public final BooleanSetting noBadSlot = add(new BooleanSetting("NoBadSlot", false, () -> page.is(Page.General)));
    public final EnumSetting<Placement> placement = add(new EnumSetting<>("Placement", Placement.Vanilla, () -> page.is(Page.General)));
    public final BooleanSetting blockCheck = add(new BooleanSetting("BlockCheck", true, () -> page.is(Page.General)));
    public final BooleanSetting oldNCP = add(new BooleanSetting("OldNCP", false, () -> page.is(Page.General)));

    public final BooleanSetting grimRotation = add(new BooleanSetting("GrimRotation", false, () -> page.is(Page.Rotation)));
    public final BooleanSetting snapBack = add(new BooleanSetting("SnapBack", true, () -> page.is(Page.Rotation)));
    public final BooleanSetting look = add(new BooleanSetting("Look", true, () -> page.is(Page.Rotation)));
    public final SliderSetting rotateTime = add(new SliderSetting("LookTime", 0.5, 0, 1, 0.01, () -> page.is(Page.Rotation)));
    public final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("SwingType", SwingSide.All));
    public final BooleanSetting noSpamRotation = add(new BooleanSetting("SpamCheck", true, () -> page.is(Page.Rotation)).setParent());
    public final SliderSetting fov = add(new SliderSetting("Fov", 10, 0, 180, 0.1, () -> page.is(Page.Rotation) && noSpamRotation.isOpen()));
    public final SliderSetting steps = add(new SliderSetting("Steps", 0.6, 0, 1, 0.01, () -> page.is(Page.Rotation)));
    public final BooleanSetting forceSync = add(new BooleanSetting("ServerSide", false, () -> page.is(Page.Rotation)));

    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", false, () -> page.is(Page.Server)));
    public final BooleanSetting applyYaw = add(new BooleanSetting("ApplyYaw", false, () -> !rotate.getValue() && page.is(Page.Server)));
    public final BooleanSetting slot = add(new BooleanSetting("Slot", false, () -> page.is(Page.Server)));

    public final BooleanSetting obsMode = add(new BooleanSetting("OBSServer", false, () -> page.is(Page.Misc)));
    public final BooleanSetting inventorySync = add(new BooleanSetting("InventorySync", false, () -> page.is(Page.Misc)));

    private final Timer applyTimer = new Timer();

    public enum Page {
        General,
        Rotation,
        Server,
        Misc
    }
    public AntiCheat() {
        super("AntiCheat", Category.Client);
        setChinese("反作弊选项");
        Ripple.EVENT_BUS.subscribe(this);
        INSTANCE = this;
    }

    @Override
    public void onLogin() {
        applyTimer.reset();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!applyTimer.passed(1000)) return;
        if (nullCheck()) return;
        if (!slot.getValue() && event.getPacket() instanceof UpdateSelectedSlotS2CPacket packet) {
            event.setCancelled(true);
            if (packet.getSlot() != mc.player.getInventory().selectedSlot) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }
    }

    public static double getOffset() {
        if (INSTANCE != null) return INSTANCE.boxSize.getValue() / 2;
        return 0.3;
    }

    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
