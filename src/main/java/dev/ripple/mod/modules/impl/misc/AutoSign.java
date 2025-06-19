package dev.ripple.mod.modules.impl.misc;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.OpenScreenEvent;
import dev.ripple.asm.accessors.IAbstractSignEditScreen;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.StringSetting;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

public class AutoSign extends Module {
    public static AutoSign INSTANCE;
    public final BooleanSetting preferPM = add(new BooleanSetting("PreferPriceMarker", true));
    private final StringSetting first = add(new StringSetting("1stRow", "%NAME%"));
    private final StringSetting second = add(new StringSetting("2ndRow", "was here"));
    private final StringSetting third = add(new StringSetting("3rdRow", "%TIME%"));
    private final StringSetting fourth = add(new StringSetting("4thRow", "%DATE%"));
    private final StringSetting dateFormat = add(new StringSetting("DateFormat", "dd/MM/yyyy",
            () -> first.getValue().contains("%DATE%") || second.getValue().contains("%DATE%")
                    || third.getValue().contains("%DATE%") || fourth.getValue().contains("%DATE%")));

    public AutoSign() {
        super("AutoSign", Category.Misc);
        setChinese("自动留言");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (preferPM.getValue() && PriceMarker.INSTANCE.isOn()) disable();
    }

    @Override
    public void onUpdate() {
        if (preferPM.getValue() && PriceMarker.INSTANCE.isOn()) disable();
    }

    private String getString(String str) {
        String format = "dd/MM/yyyy";
        try {
            format = new SimpleDateFormat(dateFormat.getValue()).format(new Date());
        } catch (Exception e) {
            CommandManager.sendChatMessage("Wrong date format!");
        }
        return str.replace("%NAME%", mc.getSession().getUsername()).replace("%TIME%", getTime()).replace("%DATE%", format);
    }

    private String getTime() {
        LocalTime time = LocalTime.now();
        return getNumber(time.getHour()) + ":" + getNumber(time.getMinute()) + ":" + getNumber(time.getSecond());
    }

    private String getNumber(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (nullCheck()) return;
        if (event.screen instanceof AbstractSignEditScreen) {
            SignBlockEntity sign = ((IAbstractSignEditScreen) event.screen).getSign();
            mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(sign.getPos(), true,
                    getString(first.getValue()), getString(second.getValue()),
                    getString(third.getValue()), getString(fourth.getValue())));
            event.cancel();
        }
    }
}