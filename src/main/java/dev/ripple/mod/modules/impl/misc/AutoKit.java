package dev.ripple.mod.modules.impl.misc;

import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.mod.modules.settings.impl.StringSetting;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoKit extends Module {
    private final StringSetting command = add(new StringSetting("Command","kit 1"));

    private final BooleanSetting infiniteTotem = add(new BooleanSetting("InfiniteTotem",false).setParent());
    public final SliderSetting delay = add(new SliderSetting("Delay", 300, 0, 500, 1, () -> infiniteTotem.isOpen()));
    public final SliderSetting playerMaxY = add(new SliderSetting("PlayerMaxY", 50, -100, 400, 1, () -> infiniteTotem.isOpen()));
    public final SliderSetting clickSlot = add(new SliderSetting("ClickSlot", 15, 0, 100, 1, () -> infiniteTotem.isOpen()));
    public final BooleanSetting sendCommand = add(new BooleanSetting("SendCommand", true, () -> infiniteTotem.isOpen())).setParent();
    public final SliderSetting minY = add(new SliderSetting("MinY", 41, -100, 400, 1, () -> infiniteTotem.isOpen() && sendCommand.getValue()));
    public final SliderSetting maxY = add(new SliderSetting("MaxY", 50, -100, 400, 1, () -> infiniteTotem.isOpen() && sendCommand.getValue()));
    private Timer waitTime = new Timer();
    Timer timer=new Timer();
    private boolean enteredServer = false;
    private boolean messageSent = false;

    public AutoKit() {
        super("AutoKit", Category.Misc);
    }

    @Override
    public void onLogin() {
        enteredServer = false;
        messageSent = false;
    }

//    int clickSlot = kit.getValueInt() + 9;

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (mc.isInSingleplayer()) return;

        if (!enteredServer) {
            enteredServer = true;
            waitTime.reset();
        }

        if (enteredServer && waitTime.passedS(1) && !messageSent) {
            String cmd = command.getValue();
            mc.getNetworkHandler().sendChatCommand(cmd);
            messageSent = true;
        }
        if (infiniteTotem.getValue()) {
            if(!timer.passedMs(delay.getValue()))return;
            updateCommand();
            if(mc.player.getY()>playerMaxY.getValue())return;
            if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler mxChest) {
                if(clickSlot.getValueInt() >mxChest.getInventory().size())return;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, clickSlot.getValueInt() , 0, SlotActionType.QUICK_MOVE, mc.player);
                mc.setScreen(null);
                timer.reset();
            }
        }
    }

    void updateCommand(){
        if(mc.player.getY()>maxY.getValue())return;
        if(mc.player.getY()<minY.getValue())return;
        if(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)return;
        mc.player.networkHandler.sendCommand("kit");
        timer.reset();
    }
}
