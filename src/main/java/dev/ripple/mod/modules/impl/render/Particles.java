package dev.ripple.mod.modules.impl.render;

import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;

import java.awt.*;

public class Particles extends Module {
    public static Particles INSTANCE;

    public Particles() {
        super("Particles", Category.Render);
        setChinese("粒子贴图");
        INSTANCE = this;
    }

    // Totem Particles
    public final BooleanSetting totem = add(new BooleanSetting("Totem", true).setParent());
    public final SliderSetting scaleT = add(new SliderSetting("TotemScale", 1f, 0f, 10f , 0.1f, totem::isOpen));
    public final SliderSetting velocityT = add(new SliderSetting("Velocity", 100, 0, 500, 1, totem::isOpen).setSuffix("%"));
    public final ColorSetting colorT = add(new ColorSetting("TotemColor1", new Color(0, 255, 255, 255), totem::isOpen));
    public final ColorSetting color2T = add(new ColorSetting("TotemColor2", new Color(175, 225, 225, 255), totem::isOpen));

    // Firework Rocket Particles
    public final BooleanSetting rocket = add(new BooleanSetting("Rocket", true).setParent());
    public final SliderSetting scaleR = add(new SliderSetting("RocketScale", 1f, 0f, 10f , 0.1f, rocket::isOpen));
    public final SliderSetting velocityR = add(new SliderSetting("RocketVelocity", 100, 0, 500, 1, rocket::isOpen).setSuffix("%"));
    public final ColorSetting colorR = add(new ColorSetting("RocketColor", new Color(0, 255, 255, 255), rocket::isOpen));

    // Attack Particles
    public final BooleanSetting attack = add(new BooleanSetting("Attack", true).setParent());
    // Sweeping Edge Particles
    public final BooleanSetting sweep = add(new BooleanSetting("Sweep", true, attack::isOpen).setParent());
    public final SliderSetting scaleS = add(new SliderSetting("SweepScale", 1f, 0f, 10f , 0.1f, () -> sweep.isOpen() && sweep.visibility.getAsBoolean()));
    public final ColorSetting colorS = add(new ColorSetting("SweepColor", new Color(0, 200, 255, 255), () -> sweep.isOpen() && sweep.visibility.getAsBoolean()));
    // Damage Particles - 未完成
    public final BooleanSetting crit = add(new BooleanSetting("Damage", true, attack::isOpen).setParent());
    public final SliderSetting scaleC = add(new SliderSetting("DamageScale", 1f, 0f, 10f , 0.1f, () -> crit.isOpen() && crit.visibility.getAsBoolean()));
    public final SliderSetting velocityC = add(new SliderSetting("DamageVelocity", 100, 0, 500, 1, () -> crit.isOpen() && crit.visibility.getAsBoolean()).setSuffix("%"));
    public final ColorSetting colorC = add(new ColorSetting("DamageColor", new Color(0, 200, 255, 255), () -> crit.isOpen() && crit.visibility.getAsBoolean()));

    // Large Explosion Particles
    public final BooleanSetting explosion = add(new BooleanSetting("Explosion", false).setParent());
    public final SliderSetting scaleE = add(new SliderSetting("ExplosionScale", 1f, 0f, 10f , 0.1f, explosion::isOpen));
    public final ColorSetting colorE = add(new ColorSetting("ExplosionColor", new Color(100, 225, 225, 255), explosion::isOpen));

    // Explosion Smoke Particles
    public final BooleanSetting eSmoke = add(new BooleanSetting("ExplosionSmoke", true).setParent());
    public final SliderSetting scaleES = add(new SliderSetting("ESmokeScale", 1f, 0f, 10f , 0.1f, eSmoke::isOpen));
    public final SliderSetting velocityES = add(new SliderSetting("ESmokeVelocity", 100, 0, 500, 1, eSmoke::isOpen).setSuffix("%"));
    public final ColorSetting colorES = add(new ColorSetting("ESmokeColor", new Color(0, 255, 255, 255), eSmoke::isOpen));

    // Portal Particles
    public final BooleanSetting portal = add(new BooleanSetting("Portal", true).setParent());
    public final SliderSetting scaleP = add(new SliderSetting("PortalScale", 1f, 0f, 10f , 0.1f, portal::isOpen));
    public final SliderSetting velocityP = add(new SliderSetting("PortalVelocity", 100, 0, 500, 1, portal::isOpen).setSuffix("%"));
    public final ColorSetting colorP = add(new ColorSetting("PortalColor", new Color(0, 175, 255, 255), portal::isOpen));
}
