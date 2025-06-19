package dev.ripple.mod.modules.impl.render;

import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.mod.modules.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.awt.*;


public class Ambience extends Module {

    public static Ambience INSTANCE;
    public final ColorSetting worldColor = add(new ColorSetting("WorldColor", new Color(0xFFFFFFFF, true)).injectBoolean(true));
    public final BooleanSetting customTime = add(new BooleanSetting("CustomTime", false).setParent());
    private final SliderSetting time = add(new SliderSetting("Time", 0, 0, 24000, customTime::isOpen));
    public final ColorSetting fog = add(new ColorSetting("FogColor", new Color(0xCC7DD5)).injectBoolean(false));
    public final ColorSetting sky = add(new ColorSetting("SkyColor", new Color(0x000000)).injectBoolean(false));
    public final ColorSetting cloud = add(new ColorSetting("CloudColor", new Color(0x000000)).injectBoolean(false));
    public final ColorSetting dimensionColor = add(new ColorSetting("DimensionColor", new Color(0x000000)).injectBoolean(false));
    public final BooleanSetting fogDistance = add(new BooleanSetting("FogDistance", false).setParent());
    public final SliderSetting fogStart = add(new SliderSetting("FogStart", 50, 0, 1000, fogDistance::isOpen));
    public final SliderSetting fogEnd = add(new SliderSetting("FogEnd", 100, 0, 1000, fogDistance::isOpen));
    public final BooleanSetting fullBright = add(new BooleanSetting("FullBright", false).setParent());
    public final SliderSetting minBright = add(new SliderSetting("MinBright", 0.5, 0, 1, 0.01, fullBright::isOpen));
    public final BooleanSetting forceOverworld = add(new BooleanSetting("ForceOverworld", false));
    public final BooleanSetting customLuminance = add(new BooleanSetting("CustomLuminance", false).setParent().injectTask(() -> {
        if (!nullCheck()) {
            mc.worldRenderer.reload();
        }
    }));
    public final SliderSetting luminance = add(new SliderSetting("Luminance", 15, 0, 15, customLuminance::isOpen).injectTask(() -> {
        if (!nullCheck() && customLuminance.getValue()) {
            mc.worldRenderer.reload();
        }
    }));

    public Ambience() {
        super("Ambience", "Custom ambience", Category.Render);
        setChinese("自定义环境");
        INSTANCE = this;
    }

    long oldTime;

    @Override
    public void onUpdate() {
        if (customTime.getValue()) {
            mc.world.setTimeOfDay((long) this.time.getValue());
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        oldTime = mc.world.getTimeOfDay();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;
        mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        mc.world.setTimeOfDay(oldTime);
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            oldTime = ((WorldTimeUpdateS2CPacket) event.getPacket()).getTime();
            event.cancel();
        }
    }
}