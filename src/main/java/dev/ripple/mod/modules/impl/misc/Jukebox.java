package dev.ripple.mod.modules.impl.misc;

import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BindSetting;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class Jukebox extends Module {
    public static Jukebox INSTANCE;
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.List));
    private final SliderSetting volume = add(new SliderSetting("Volume", 100, 0, 100).setSuffix("%"));
    private final SliderSetting delay = add(new SliderSetting("Delay", 5, 0, 20).setSuffix("s"));
    private final BindSetting left = add(new BindSetting("Left", GLFW.GLFW_KEY_LEFT));
    private final BindSetting right = add(new BindSetting("Right", GLFW.GLFW_KEY_RIGHT));
    private final SliderSetting cDelay = add(new SliderSetting("ClickDelay", 300, 0, 2000).setSuffix("ms"));
    private final BooleanSetting showName = add(new BooleanSetting("ShowName", true));
    private final BooleanSetting showTime = add(new BooleanSetting("ShowTime", true));
    private final SliderSetting r = add(new SliderSetting("Record", 0, 0, 18));
    //private final BooleanSetting custom = add(new BooleanSetting("Custom", true));
    private int index = r.getValueInt();
    private SoundInstance instance = null;
    private final Timer timer = new Timer();
    private final Timer click = new Timer();
    private int duration = 0;
    private final SoundEvent[] discs = {
            SoundEvents.MUSIC_DISC_13.value(),                  // Alpha
            SoundEvents.MUSIC_DISC_CAT.value(),                 // Alpha
            SoundEvents.MUSIC_DISC_BLOCKS.value(),              // Beta 1.9
            SoundEvents.MUSIC_DISC_CHIRP.value(),               // Beta 1.9
            SoundEvents.MUSIC_DISC_FAR.value(),                 // Beta 1.9
            SoundEvents.MUSIC_DISC_MALL.value(),                // Beta 1.9
            SoundEvents.MUSIC_DISC_MELLOHI.value(),             // Beta 1.9
            SoundEvents.MUSIC_DISC_STAL.value(),                // Beta 1.9
            SoundEvents.MUSIC_DISC_STRAD.value(),               // Beta 1.9
            SoundEvents.MUSIC_DISC_WARD.value(),                // Beta 1.9
            SoundEvents.MUSIC_DISC_11.value(),                  // Beta 1.9
            SoundEvents.MUSIC_DISC_WAIT.value(),                // 1.4.0
            SoundEvents.MUSIC_DISC_PIGSTEP.value(),             // 1.16（Piglin）
            SoundEvents.MUSIC_DISC_OTHERSIDE.value(),           // 1.18（Caves and Cliffs）
            SoundEvents.MUSIC_DISC_5.value(),                   // 1.19（Deep Dark）
            SoundEvents.MUSIC_DISC_RELIC.value(),               // 1.20 (Trails & Tales)
            SoundEvents.MUSIC_DISC_CREATOR.value(),             // 1.21 (Tricky Trials)
            SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX.value(),   // 1.21 (Tricky Trials)
            SoundEvents.MUSIC_DISC_PRECIPICE.value()            // 1.21 (Tricky Trials)
    };

    private static final Map<SoundEvent, Integer> discDurations = Map.ofEntries(
            Map.entry(SoundEvents.MUSIC_DISC_13.value(), 178),
            Map.entry(SoundEvents.MUSIC_DISC_CAT.value(), 185),
            Map.entry(SoundEvents.MUSIC_DISC_BLOCKS.value(), 345),
            Map.entry(SoundEvents.MUSIC_DISC_CHIRP.value(), 185),
            Map.entry(SoundEvents.MUSIC_DISC_FAR.value(), 174),
            Map.entry(SoundEvents.MUSIC_DISC_MALL.value(), 197),
            Map.entry(SoundEvents.MUSIC_DISC_MELLOHI.value(), 96),
            Map.entry(SoundEvents.MUSIC_DISC_STAL.value(), 150),
            Map.entry(SoundEvents.MUSIC_DISC_STRAD.value(), 188),
            Map.entry(SoundEvents.MUSIC_DISC_WARD.value(), 251),
            Map.entry(SoundEvents.MUSIC_DISC_11.value(), 71),
            Map.entry(SoundEvents.MUSIC_DISC_WAIT.value(), 238),
            Map.entry(SoundEvents.MUSIC_DISC_PIGSTEP.value(), 149),
            Map.entry(SoundEvents.MUSIC_DISC_OTHERSIDE.value(), 195),
            Map.entry(SoundEvents.MUSIC_DISC_5.value(), 178),
            Map.entry(SoundEvents.MUSIC_DISC_RELIC.value(), 218),
            Map.entry(SoundEvents.MUSIC_DISC_CREATOR.value(), 176),
            Map.entry(SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX.value(), 73),
            Map.entry(SoundEvents.MUSIC_DISC_PRECIPICE.value(), 299)
    );

    private static final Map<SoundEvent, String> discNames = Map.ofEntries(
            Map.entry(SoundEvents.MUSIC_DISC_13.value(), "13"),
            Map.entry(SoundEvents.MUSIC_DISC_CAT.value(), "Cat"),
            Map.entry(SoundEvents.MUSIC_DISC_BLOCKS.value(), "Blocks"),
            Map.entry(SoundEvents.MUSIC_DISC_CHIRP.value(), "Chirp"),
            Map.entry(SoundEvents.MUSIC_DISC_FAR.value(), "Far"),
            Map.entry(SoundEvents.MUSIC_DISC_MALL.value(), "Mall"),
            Map.entry(SoundEvents.MUSIC_DISC_MELLOHI.value(), "Mellohi"),
            Map.entry(SoundEvents.MUSIC_DISC_STAL.value(), "Stal"),
            Map.entry(SoundEvents.MUSIC_DISC_STRAD.value(), "Strad"),
            Map.entry(SoundEvents.MUSIC_DISC_WARD.value(), "Ward"),
            Map.entry(SoundEvents.MUSIC_DISC_11.value(), "11"),
            Map.entry(SoundEvents.MUSIC_DISC_WAIT.value(), "Wait"),
            Map.entry(SoundEvents.MUSIC_DISC_PIGSTEP.value(), "Pigstep"),
            Map.entry(SoundEvents.MUSIC_DISC_OTHERSIDE.value(), "Otherside"),
            Map.entry(SoundEvents.MUSIC_DISC_5.value(), "5"),
            Map.entry(SoundEvents.MUSIC_DISC_RELIC.value(), "Relic"),
            Map.entry(SoundEvents.MUSIC_DISC_CREATOR.value(), "Creator"),
            Map.entry(SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX.value(), "Creator_MusicBox"),
            Map.entry(SoundEvents.MUSIC_DISC_PRECIPICE.value(), "Precipice")
    );


    public enum Mode {
        List, Random, Repeat
    }

    public Jukebox() {
        super("Jukebox", Category.Misc);
        setChinese("唱片机");
        INSTANCE = this;
        r.hide();
    }

    @Override
    public String getInfo() {
        if (index > -1 && index < discs.length) {
            long passedMs = timer.getPassedTimeMs();
            int seconds = (int) (passedMs / 1000);
            if (seconds > duration) seconds = 0;
            int minutes = seconds / 60;
            int secPart = seconds % 60;

            if (showName.getValue() && !showTime.getValue()) {
                return discNames.get(discs[index]);
            } else if (!showName.getValue() && showTime.getValue()){
                return String.format("%02d:%02d", minutes, secPart);
            } else if (showName.getValue()) {
                return discNames.get(discs[index]) + " - " + String.format("%02d:%02d", minutes, secPart);
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        if (instance != null) {
            mc.getSoundManager().stop(instance); // Stop playing the previous disc.
            instance = null;
        }
        r.setValue(index);
    }

    @Override
    public void onEnable() {
        index = r.getValueInt();
        timer.reset();
        duration = -delay.getValueInt();
        if (instance != null) {
            mc.getSoundManager().stop(instance); // Stop playing the previous disc.
            instance = null;
        }
    }

    @Override
    public void onLogin() {
        timer.reset();
    }

    private boolean manual = false;

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (mc.currentScreen == null) {
            if (left.isPressed() && click.passedMs(cDelay.getValueInt())) {
                index--; // Change the index.
                timer.setMs(duration * 1000L); // Make the timer pass the duration.
                click.reset();
                manual = true;
                if (instance != null) mc.getSoundManager().stop(instance); // Stop playing the previous disc.
            } else if (right.isPressed() && click.passedMs(cDelay.getValueInt())) {
                index++;
                timer.setMs(duration * 1000L);
                click.reset();
                manual = true;
                if (instance != null) mc.getSoundManager().stop(instance);
            }
        }

        if (!timer.passedS(duration)) {
            if (instance != null) {
                if (!mc.getSoundManager().isPlaying(instance)) {
                    mc.getSoundManager().play(instance); // Replay the music when it's not playing.
                    timer.reset();
                }
            }
            return;
        }

        if (!timer.passedS(duration + delay.getValueInt())) { // Check the sound duration.
            return;
        }

        // Modes
        if (!manual) {
            if (mode.is(Mode.List)) {
                index++;
            } else if (mode.is(Mode.Random)) {
                index = new java.util.Random().nextInt(discs.length);
            } //else {
                // Do nothing when mode.is(Repeat).
            //}
        }

        if (index > discs.length - 1) {
            index = 0; // Return to the first disc if every disc is played.
        }
        if (index < 0) {
            index = discs.length - 1; // Return to the last disc if index is less than 0.
        }

        SoundEvent disc = discs[index]; // Get the disc from the index.
        instance = new PositionedSoundInstance(
                disc,
                SoundCategory.MASTER,
                (float) volume.getValueInt() / 100,  // volume
                1.0f,  // pitch
                SoundInstance.createRandom(), // idk, just random stuff
                false, // repeat
                0, // repeat delay
                SoundInstance.AttenuationType.NONE,
                mc.player.getX(),
                mc.player.getY() + 114514,
                mc.player.getZ()
        );
        mc.getSoundManager().play(instance);

        duration = discDurations.get(disc); // Reset the duration.
        timer.reset(); // Start timing.
        manual = false;
        r.setValue(index);
    }
}