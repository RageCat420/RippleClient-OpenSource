package dev.ripple.mod.modules.impl.render;

import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.HeldItemRendererEvent;
import dev.ripple.asm.accessors.IHeldItemRenderer;
import dev.ripple.mod.modules.Module;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModel extends Module {
    public static ViewModel INSTANCE;
    public ViewModel() {
        super("ViewModel", Category.Render);
        setChinese("手持模型");
        INSTANCE = this;
    }

    public final BooleanSetting onePointTwelve = add(new BooleanSetting("1.12", true));
    public final BooleanSetting swingAnimation = add(new BooleanSetting("SwingAnimation", false));
    public final BooleanSetting eatAnimation = add(new BooleanSetting("EatAnimation", false));
    public final BooleanSetting mainhandSwap = add(new BooleanSetting("MainhandSwap", true));
    public final BooleanSetting offhandSwap = add(new BooleanSetting("OffhandSwap", true));
    public final SliderSetting scaleMainX = add(new SliderSetting("ScaleMainX", 1f, 0.1f, 5f, 0.01));
    public final SliderSetting scaleMainY = add(new SliderSetting("ScaleMainY", 1f, 0.1f, 5f, 0.01));
    public final SliderSetting scaleMainZ = add(new SliderSetting("ScaleMainZ", 1f, 0.1f, 5f, 0.01));
    public final SliderSetting positionMainX = add(new SliderSetting("PositionMainX", 0f, -3.0f, 3f, 0.01));
    public final SliderSetting positionMainY = add(new SliderSetting("PositionMainY", 0f, -3.0f, 3f, 0.01));
    public final SliderSetting positionMainZ = add(new SliderSetting("PositionMainZ", 0f, -3.0f, 3f, 0.01));
    public final SliderSetting rotationMainX = add(new SliderSetting("RotationMainX", 0f, -180.0f, 180f, 0.01));
    public final SliderSetting rotationMainY = add(new SliderSetting("RotationMainY", 0f, -180.0f, 180f, 0.01));
    public final SliderSetting rotationMainZ = add(new SliderSetting("RotationMainZ", 0f, -180.0f, 180f, 0.01));
    public final SliderSetting scaleOffX = add(new SliderSetting("ScaleOffX", 1f, 0.1f, 5f, 0.01));
    public final SliderSetting scaleOffY = add(new SliderSetting("ScaleOffY", 1f, 0.1f, 5f, 0.01));
    public final SliderSetting scaleOffZ = add(new SliderSetting("ScaleOffZ", 1f, 0.1f, 5f, 0.01));
    public final SliderSetting positionOffX = add(new SliderSetting("PositionOffX", 0f, -3.0f, 3f, 0.01));
    public final SliderSetting positionOffY = add(new SliderSetting("PositionOffY", 0f, -3.0f, 3f, 0.01));
    public final SliderSetting positionOffZ = add(new SliderSetting("PositionOffZ", 0f, -3.0f, 3f, 0.01));
    public final SliderSetting rotationOffX = add(new SliderSetting("RotationOffX", 0f, -180.0f, 180f, 0.01));
    public final SliderSetting rotationOffY = add(new SliderSetting("RotationOffY", 0f, -180.0f, 180f, 0.01));
    public final SliderSetting rotationOffZ = add(new SliderSetting("RotationOffZ", 0f, -180.0f, 180f, 0.01));
    public final BooleanSetting slowAnimation = add(new BooleanSetting("SlowAnimation", true));
    public final SliderSetting slowAnimationVal = add(new SliderSetting("SlowValue", 6, 1, 50));
    public final SliderSetting eatX = add(new SliderSetting("EatX", 1f, -1f, 2f, 0.01));
    public final SliderSetting eatY = add(new SliderSetting("EatY", 1f, -1f, 2f, 0.01));
    private final Timer timer = new Timer();

    @EventHandler
    public void onPacketSent(PacketEvent.Sent event) {
        if (!timer.passedMs(300)) return;
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket
                || event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            if (mc.player.handSwinging && onePointTwelve.getValue()) {
                mc.player.handSwinging = false;
                if (//mc.player.getActiveHand() == Hand.MAIN_HAND &&
                        offhandSwap.getValue()) {
                    ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(0f);
                    ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(mc.player.getOffHandStack());
                } else if (//mc.player.getActiveHand() == Hand.OFF_HAND &&
                        mainhandSwap.getValue()) {
                    ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(0f);
                    ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(mc.player.getMainHandStack());
                }
                timer.reset();
                //mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
                //mc.player.swingHand(mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, false);
            }
        }

        if (event.getPacket() instanceof CraftRequestC2SPacket && onePointTwelve.getValue()) {
            if (mainhandSwap.getValue()) {
                ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(0f);
                ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(mc.player.getMainHandStack());
            }
            if (offhandSwap.getValue()) {
                ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(0f);
                ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(mc.player.getOffHandStack());
            }
            timer.reset();
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (!mainhandSwap.getValue() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressMainHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(mc.player.getMainHandStack());
        }

        if (!offhandSwap.getValue() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressOffHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(mc.player.getOffHandStack());
        }
    }

    @EventHandler
    private void onHeldItemRender(HeldItemRendererEvent event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            event.getStack().translate(positionMainX.getValueFloat(), positionMainY.getValueFloat(), positionMainZ.getValueFloat());
            event.getStack().scale(scaleMainX.getValueFloat(), scaleMainY.getValueFloat(), scaleMainZ.getValueFloat());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationMainX.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationMainY.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationMainZ.getValueFloat()));
        } else {
            event.getStack().translate(positionOffX.getValueFloat(), positionOffY.getValueFloat(), positionOffZ.getValueFloat());
            event.getStack().scale(scaleOffX.getValueFloat(), scaleOffY.getValueFloat(), scaleOffZ.getValueFloat());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationOffX.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationOffY.getValueFloat()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffZ.getValueFloat()));
        }
    }
}
