package dev.ripple.mod.modules.impl.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ripple.Ripple;
import dev.ripple.api.utils.render.TextUtil;
import dev.ripple.mod.gui.font.FontRenderers;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.world.World;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HUD extends Module {
    public static HUD INSTANCE;
    public final BooleanSetting armor = add(new BooleanSetting("Armor", true));
    public final BooleanSetting up = add(new BooleanSetting("Up", false));
    public final BooleanSetting customFont = add(new BooleanSetting("CustomFont", true));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(0, 0, 208)));
    public final ColorSetting pulse = add(new ColorSetting("Pulse", new Color(0, 0, 79)).injectBoolean(true));
    public final BooleanSetting waterMark = add(new BooleanSetting("WaterMark", true));
    public final StringSetting waterMarkString = add(new StringSetting("Title", "%hackname% %version%"));
    public final SliderSetting offset = add(new SliderSetting("Offset", 1, 0, 100, -1));
    public final BooleanSetting sync = add(new BooleanSetting("InfoColorSync", true));
    public final BooleanSetting lowerCase = add(new BooleanSetting("LowerCase", false));
    public final BooleanSetting fps = add(new BooleanSetting("FPS", true));
    public final BooleanSetting ping = add(new BooleanSetting("Ping", true));
    public final BooleanSetting tps = add(new BooleanSetting("TPS", true));
    public final BooleanSetting irc = add(new BooleanSetting("IRC", true));
    public final BooleanSetting ip = add(new BooleanSetting("IP", false));
    public final BooleanSetting time = add(new BooleanSetting("Time", false));
    public final BooleanSetting speed = add(new BooleanSetting("Speed", true));
    public final BooleanSetting brand = add(new BooleanSetting("Brand", false));
    public final BooleanSetting potions = add(new BooleanSetting("Potions", true));
    public final BooleanSetting coords = add(new BooleanSetting("Coords", true));
    private final SliderSetting pulseSpeed = add(new SliderSetting("PulseSpeed", 1, 0, 5, 0.1));
    private final SliderSetting pulseCounter = add(new SliderSetting("PulseCounter", 10, 1, 50));
    public HUD() {
        super("HUD", Category.Client);
        setChinese("界面");
        INSTANCE = this;
    }

    private final DecimalFormat decimal = new DecimalFormat("0.0");

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (armor.getValue()) {
            Ripple.GUI.armorHud.draw(drawContext, tickDelta, null);
        }
        if (waterMark.getValue()) {
            if (pulse.booleanValue) {
                TextUtil.drawStringPulse(drawContext, waterMarkString.getValue().replaceAll("%version%", Ripple.VERSION).replaceAll("%hackname%", Ripple.NAME), offset.getValueInt(), offset.getValueInt(), color.getValue(), pulse.getValue(), pulseSpeed.getValue(), pulseCounter.getValueInt(), customFont.getValue());
            } else {
                TextUtil.drawString(drawContext, waterMarkString.getValue().replaceAll("%version%", Ripple.VERSION).replaceAll("%hackname%", Ripple.NAME), offset.getValueInt(), offset.getValueInt(), color.getValue().getRGB(), customFont.getValue());
            }
        }
        int fontHeight = getHeight();
        int height;
        int y;
        if (up.getValue()) {
            y = 1;
            height = -fontHeight;
        } else {
            y = mc.getWindow().getScaledHeight() - fontHeight;
            if (mc.currentScreen instanceof ChatScreen) {
                y -= 15;
            }
            height = fontHeight;
        }
        int windowWidth = mc.getWindow().getScaledWidth() - 1;
        if (potions.getValue()) {
            List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
            for (StatusEffectInstance potionEffect : effects) {
                StatusEffect potion = potionEffect.getEffectType().value();
                String power = "";
                switch (potionEffect.getAmplifier()) {
                    case 0 -> power = "I";
                    case 1 -> power = "II";
                    case 2 -> power = "III";
                    case 3 -> power = "IV";
                    case 4 -> power = "V";
                }
                String s = potion.getName().getString() + " " + power;
                String s2 = getDuration(potionEffect);
                String text = s + " " + s2;
                int x = getWidth(text);
                TextUtil.drawString(drawContext, text, windowWidth - x, y, potionEffect.getEffectType().value().getColor(), customFont.getValue());
                y -= height;
            }
        }
        if (brand.getValue()) {
            String brand = (mc.isInSingleplayer() ? "Vanilla" : mc.getNetworkHandler().getBrand().replaceAll("\\(.*?\\)", ""));
            int x = getWidth("ServerBrand " + brand);
            drawText(drawContext, "ServerBrand §f" + brand, windowWidth - x, y);
            y -= height;
        }
        if (time.getValue()) {
            String text = "Time §f" + (new SimpleDateFormat("h:mm a", Locale.ENGLISH)).format(new Date());
            int width = getWidth(text);
            drawText(drawContext, text, windowWidth - width, y);
            y -= height;
        }
        if (ip.getValue()) {
            int x = getWidth("Server " + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getCurrentServerEntry().address));
            drawText(drawContext, "Server §f" + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getCurrentServerEntry().address), windowWidth - x, y);
            y -= height;
        }
        if (irc.getValue()) {
            int x = getWidth("IRC Online: " + Ripple.IRC.getOnlinePlayers());
            drawText(drawContext, "IRC Online: " + Ripple.IRC.getOnlinePlayers(), windowWidth - x, y);
            y -= height;
        }
        if (tps.getValue()) {
            int x = getWidth("TPS " + Ripple.SERVER.getTPS() + " [" + Ripple.SERVER.getCurrentTPS() + "]");
            drawText(drawContext, "TPS §f" + Ripple.SERVER.getTPS() + " §7[§f" + Ripple.SERVER.getCurrentTPS() + "§7]", windowWidth - x, y);
            y -= height;
        }
        if (speed.getValue()) {
            double x = mc.player.getX() - mc.player.prevX;
            // double y = mc.player.getY() - mc.player.prevY;
            double z = mc.player.getZ() - mc.player.prevZ;
            double dist = Math.sqrt(x * x + z * z) / 1000.0;
            double div = 0.05 / 3600.0;
            float timer = Ripple.TIMER.get();
            final double speed = dist / div * timer;
            String text = String.format("Speed §f%skm/h",
                    decimal.format(speed));
            int width = getWidth(text);
            drawText(drawContext, text, windowWidth - width, y);
            y -= height;
        }
        if (fps.getValue()) {
            int x = getWidth("FPS " + Ripple.FPS.getFps());
            drawText(drawContext, "FPS §f" + Ripple.FPS.getFps(), windowWidth - x, y);
            y -= height;
        }
        if (ping.getValue()) {
            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            String ping;
            if (playerListEntry == null) {
                ping = "Unknown";
            } else {
                ping = String.valueOf(playerListEntry.getLatency());
            }
            int x = getWidth("Ping " + ping);
            drawText(drawContext, "Ping §f" + ping, windowWidth - x, y);
            y -= height;
        }

        if (coords.getValue()) {
            boolean inNether = mc.world.getRegistryKey().equals(World.NETHER);

            int posX = mc.player.getBlockX();
            int posY = mc.player.getBlockY();
            int posZ = mc.player.getBlockZ();

            float factor = !inNether ? 0.125F : 8.0F;

            int anotherWorldX = (int) (mc.player.getX() * factor);
            int anotherWorldZ = (int) (mc.player.getZ() * factor);

            String coordsString = "XYZ §f" + (inNether ? (posX + ", " + posY + ", " + posZ + " §7[§f" + anotherWorldX + ", " + anotherWorldZ + "§7]§f") : (posX + ", " + posY + ", " + posZ + "§7 [§f" + anotherWorldX + ", " + anotherWorldZ + "§7]"));

            drawText(drawContext, coordsString, (int) 2.0F, mc.getWindow().getScaledHeight() - fontHeight - (mc.currentScreen instanceof ChatScreen ? 15 : 0));
        }
    }

    private int getWidth(String s) {
        if (customFont.getValue()) {
            return (int) FontRenderers.ui.getWidth(s);
        }
        return mc.textRenderer.getWidth(s);
    }

    private int getHeight() {
        if (customFont.getValue()) {
            return (int) FontRenderers.ui.getFontHeight();
        }
        return mc.textRenderer.fontHeight;
    }

    private void drawText(DrawContext drawContext, String s, int x, int y) {
        if (sync.getValue()) {
            ModuleList.INSTANCE.counter--;
            if (lowerCase.getValue()) {
                s = s.toLowerCase();
            }
            TextUtil.drawString(drawContext, s, x, y, ModuleList.INSTANCE.getColor(ModuleList.INSTANCE.counter), customFont.getValue());
            return;
        }
        if (pulse.booleanValue) {
            TextUtil.drawStringPulse(drawContext, s, x, y, color.getValue(), pulse.getValue(), pulseSpeed.getValue(), pulseCounter.getValueInt(), customFont.getValue());
        } else {
            TextUtil.drawString(drawContext, s, x, y, color.getValue().getRGB(), customFont.getValue());
        }
    }

    public static String getDuration(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else {
            int var1 = pe.getDuration();
            int mins = var1 / 1200;
            int sec = (var1 % 1200) / 20;

            return mins + ":" + sec;
        }
    }

}
