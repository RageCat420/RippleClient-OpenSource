package dev.ripple.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.HeldItemRendererEvent;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;

public class Chams extends Module {
    public static Chams INSTANCE;

    public Chams() {
        super("Chams", Category.Render);
        setChinese("模型上色");
        INSTANCE = this;
    }

    //Crystal
    public final BooleanSetting crystal = add(new BooleanSetting("Crystal", true).setParent());
    public final ColorSetting core = add(new ColorSetting("Core", new Color(255, 255, 255, 255), crystal::isOpen).injectBoolean(true));
    public final ColorSetting outerFrame = add(new ColorSetting("OuterFrame", new Color(255, 255, 255, 255), crystal::isOpen).injectBoolean(true));
    public final ColorSetting innerFrame = add(new ColorSetting("InnerFrame", new Color(255, 255, 255, 255), crystal::isOpen).injectBoolean(true));
    public final BooleanSetting texture = add(new BooleanSetting("Texture", true, crystal::isOpen));
    public final SliderSetting scale = add(new SliderSetting("Scale", 1, 0, 3f, 0.01, crystal::isOpen));
    public final SliderSetting spinValue = add(new SliderSetting("SpinSpeed", 1f, 0, 3f, 0.01, crystal::isOpen));
    public final SliderSetting bounceHeight = add(new SliderSetting("BounceHeight", 1, 0, 3f, 0.01, crystal::isOpen));
    public final SliderSetting floatValue = add(new SliderSetting("BounceSpeed", 1f, 0, 3f, 0.01, crystal::isOpen));
    public final SliderSetting floatOffset = add(new SliderSetting("YOffset", 0f, -1, 1f, 0.01, crystal::isOpen));
    //ThroughWall
    public final BooleanSetting throughWall = add(new BooleanSetting("ThroughWall", true).setParent());
    public final BooleanSetting crystals = add(new BooleanSetting("Crystals", true, throughWall::isOpen));
    public final ColorSetting slimes = add(new ColorSetting("Slimes", new Color(255, 255, 255, 175), throughWall::isOpen).injectBoolean(true));
    public final ColorSetting players = add(new ColorSetting("Players", new Color(255, 255, 255, 175), throughWall::isOpen).injectBoolean(true));
    public final ColorSetting friends = add(new ColorSetting("Friends", new Color(45, 215, 255, 175), () -> throughWall.isOpen() && players.booleanValue));
    public final ColorSetting villagers = add(new ColorSetting("Villagers", new Color(255, 255, 255, 175), throughWall::isOpen).injectBoolean(true));
    public final ColorSetting animals = add(new ColorSetting("Animals", new Color(255, 255, 255, 175), throughWall::isOpen).injectBoolean(true));
    public final ColorSetting mobs = add(new ColorSetting("Mobs", new Color(255, 255, 255, 175), throughWall::isOpen).injectBoolean(true));
    //Hand
    private final ColorSetting hand = add(new ColorSetting("Hand", new Color(255, 255, 255, 125)).injectBoolean(true));

    @EventHandler
    public void onRenderHands(HeldItemRendererEvent event) {
        if (hand.booleanValue) {
            RenderSystem.setShaderColor(hand.getValue().getRed() / 255f, hand.getValue().getGreen() / 255f, hand.getValue().getBlue() / 255f, hand.getValue().getAlpha() / 255f);
        }
    }

    public boolean chams(Entity entity) {
        if (entity instanceof EndCrystalEntity) return crystals.getValue();
        if (entity instanceof SlimeEntity) return slimes.booleanValue;
        if (entity instanceof PlayerEntity) return players.booleanValue;
        if (entity instanceof VillagerEntity || entity instanceof WanderingTraderEntity) return villagers.booleanValue;
        if (entity instanceof AnimalEntity) return animals.booleanValue;
        if (entity instanceof MobEntity) return mobs.booleanValue;
        return false;
    }
}