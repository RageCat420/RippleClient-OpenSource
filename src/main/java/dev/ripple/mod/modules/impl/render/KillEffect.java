package dev.ripple.mod.modules.impl.render;

import dev.ripple.api.utils.render.Render3DUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillEffect extends Module {
    public static KillEffect INSTANCE;
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.LightningBolt));
    public final SliderSetting speed = add(new SliderSetting("Speed", 0, -10, 10, () -> mode.is(Mode.Orthodox)));
    private final BooleanSetting playSound = add(new BooleanSetting("PlaySound", true, () -> mode.is(Mode.Orthodox)).setParent());
    public final SliderSetting volume = add(new SliderSetting("Volume", 100, 0 ,100, () -> playSound.isOpen() && playSound.visibility.getAsBoolean()));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 0, 150), () -> mode.is(Mode.Orthodox)));
    private final BooleanSetting mobs = add(new BooleanSetting("Mobs", false));
    private final Map<Entity, Long> renderEntities = new ConcurrentHashMap<>();
    private final Map<Entity, Long> lightingEntities = new ConcurrentHashMap<>();

    public KillEffect() {
        super("KillEffect", Category.Render);
        INSTANCE = this;
    }

    private enum Mode {
        LightningBolt,
        Orthodox,
        FallingLava
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (mc.world == null) return;
        switch (mode.getValue()) {
            case Orthodox -> renderEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 3000) {
                    renderEntities.remove(entity);
                } else {
                    Render3DUtil.drawLine(entity.getPos().add(0, calculateSpeed(), 0), entity.getPos().add(0, 3 + calculateSpeed(), 0), color.getValue());
                    Render3DUtil.drawLine(entity.getPos().add(1, 2.3 + calculateSpeed(), 0), entity.getPos().add(-1, 2.3 + calculateSpeed(), 0), color.getValue());
                    Render3DUtil.drawLine(entity.getPos().add(0.5, 1.2 + calculateSpeed(), 0), entity.getPos().add(-0.5, 0.8 + calculateSpeed(), 0), color.getValue());
                }
            });
            case FallingLava -> renderEntities.keySet().forEach(entity -> {
                for (int i = 0; i < entity.getHeight() * 10; i++) {
                    for (int j = 0; j < entity.getWidth() * 10; j++) {
                        for (int k = 0; k < entity.getWidth() * 10; k++) {
                            mc.world.addParticle(ParticleTypes.FALLING_LAVA, entity.getX() + j * 0.1, entity.getY() + i * 0.1, entity.getZ() + k * 0.1, 0, 0, 0);
                        }
                    }
                }

                renderEntities.remove(entity);
            });
            case LightningBolt -> renderEntities.forEach((entity, time) -> {
                LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                lightningEntity.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                mc.world.addEntity(lightningEntity);
                renderEntities.remove(entity);
                lightingEntities.put(entity, System.currentTimeMillis());
            });
        }
    }

    @Override
    public void onUpdate() {
        if (mc.world == null) return;
        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity) && !mobs.getValue()) return;
            if (!(entity instanceof LivingEntity liv)) return;

            if (entity == mc.player || renderEntities.containsKey(entity) || lightingEntities.containsKey(entity))
                return;
            if (entity.isAlive() || liv.getHealth() != 0) return;

            if (playSound.getValue() && mode.getValue() == Mode.Orthodox)
                mc.world.playSound(mc.player, entity.getX(), entity.getY(), entity.getZ(), SoundEvent.of(Identifier.of("sounds/orthodox")), SoundCategory.MASTER, volume.getValueFloat() / 100f, 1f);

            renderEntities.put(entity, System.currentTimeMillis());
        });

        if (!lightingEntities.isEmpty()) {
            lightingEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 5000) {
                    lightingEntities.remove(entity);
                }
            });
        }
    }

    private double calculateSpeed() {
        return (double) speed.getValueInt() / 100;
    }
}