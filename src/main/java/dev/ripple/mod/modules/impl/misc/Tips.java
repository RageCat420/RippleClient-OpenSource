package dev.ripple.mod.modules.impl.misc;

import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.DeathEvent;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;

import java.awt.*;
import java.text.DecimalFormat;

public class Tips extends Module {
    public static Tips INSTANCE;

    public Tips() {
        super("Tips", Category.Misc);
        setChinese("提示");
        INSTANCE = this;
    }

    public final BooleanSetting deathCoords = add(new BooleanSetting("DeathCoords", true));
    public final BooleanSetting serverLag = add(new BooleanSetting("ServerLag", true));
    public final BooleanSetting lagBack = add(new BooleanSetting("LagBack", true));
    public final BooleanSetting turtle = add(new BooleanSetting("Turtle", true).setParent());
    private final SliderSetting yOffset = add(new SliderSetting("YOffset", 0, -200, 200, turtle::isOpen));
    public final BooleanSetting armor = add(new BooleanSetting("Armor", true).setParent());
    public final SliderSetting dmg = add(new SliderSetting("Damage", 30, 0, 100, armor::isOpen).setSuffix("%"));
    private final BooleanSetting sound = add(new BooleanSetting("Sound", true, armor::isOpen));
    public final BooleanSetting shulkerViewer = add(new BooleanSetting("ShulkerViewer", true));

    int turtles = 0;

    @Override
    public void onUpdate() {
        if (turtle.getValue()) {
            turtles = InventoryUtil.getPotionCount(StatusEffects.RESISTANCE);
        }
    }

    private final Timer lagTimer = new Timer();
    private final Timer lagBackTimer = new Timer();

    @EventHandler
    public void onPacketEvent(PacketEvent.Receive event) {
        lagTimer.reset();
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            lagBackTimer.reset();
        }
    }

    DecimalFormat df = new DecimalFormat("0.0");
    int color = new Color(190, 0, 0).getRGB();

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (serverLag.getValue() && lagTimer.passedS(1.4)) {
            String text = "Server not responding (" + df.format(lagTimer.getPassedTimeMs() / 1000d) + "s)";
            drawContext.drawText(mc.textRenderer, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2, 10 + mc.textRenderer.fontHeight, color, true);
        }
        if (lagBack.getValue() && !lagBackTimer.passedS(1.5)) {
            String text = "Lagback (" + df.format((1500 - lagBackTimer.getPassedTimeMs()) / 1000d) + "s)";
            drawContext.drawText(mc.textRenderer, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2, 10 + mc.textRenderer.fontHeight * 2, color, true);
        }
        if (turtle.getValue()) {
            String text = "§e" + turtles;
            if (mc.player.hasStatusEffect(StatusEffects.RESISTANCE) && mc.player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() >= 2) {
                text += " §f" + (mc.player.getStatusEffect(StatusEffects.RESISTANCE).getDuration() / 20 + 1);
            }
            drawContext.drawText(mc.textRenderer, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2, mc.getWindow().getScaledHeight() / 2 + mc.textRenderer.fontHeight - yOffset.getValueInt(), -1, true);
        }
        if (armor.getValue()) {
            DefaultedList<ItemStack> armors = mc.player.getInventory().armor;
            for (ItemStack armor : armors) {
                String text = " ";
                if (armor.isEmpty()) return;
                if (EntityUtil.getDamagePercent(armor) > dmg.getValue()) {
                    text = " ";
                    if (armor == armors.get(0)) {
                        played = false;
                    } else if (armor == armors.get(1)) {
                        played2 = false;
                    } else if (armor == armors.get(2)) {
                        played3 = false;
                    } else if (armor == armors.get(3)) {
                        played4 = false;
                    }
                }
                if (EntityUtil.getDamagePercent(armor) < dmg.getValue()) {
                    text = "§4Your armors durability is low!";
                    if (sound.getValue() && ((armor == armors.get(0) && !played)
                            || (armor == armors.get(1) && !played2)
                            || (armor == armors.get(2) && !played3)
                            || (armor == armors.get(3) && !played4))) {
                        mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 100f, 1.9f);
                        if (armor == armors.get(0)) {
                            played = true;
                        } else if (armor == armors.get(1)) {
                            played2 = true;
                        } else if (armor == armors.get(2)) {
                            played3 = true;
                        } else if (armor == armors.get(3)) {
                            played4 = true;
                        }
                    }
                }
                drawContext.drawText(mc.textRenderer, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2, 10 + mc.textRenderer.fontHeight * 3, color, true);
            }
        }
    }

    @Override
    public void onEnable() {
        played = false;
        played2 = false;
        played3 = false;
        played4 = false;
    }

    @Override
    public void onLogin() {
        played = false;
        played2 = false;
        played3 = false;
        played4 = false;
    }

    private boolean played = false;
    private boolean played2 = false;
    private boolean played3 = false;
    private boolean played4 = false;

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (deathCoords.getValue() && player == mc.player) {
            CommandManager.sendChatMessage("§4You died at " + (int) player.getX() + ", " + (int) player.getY() + ", " + (int) player.getZ());
        }
    }
}