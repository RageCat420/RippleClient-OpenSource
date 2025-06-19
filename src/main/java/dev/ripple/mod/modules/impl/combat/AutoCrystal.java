package dev.ripple.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.LookAtEvent;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.api.events.impl.Render3DEvent;
import dev.ripple.api.events.impl.UpdateWalkingPlayerEvent;
import dev.ripple.api.utils.combat.CombatUtil;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.math.*;
import dev.ripple.api.utils.render.ColorUtil;
import dev.ripple.api.utils.render.JelloUtil;
import dev.ripple.api.utils.render.Render3DUtil;
import dev.ripple.api.utils.world.BlockPosX;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.asm.accessors.IEntity;
import dev.ripple.Ripple;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.AntiCheat;
import dev.ripple.mod.modules.impl.client.ClientSetting;
import dev.ripple.mod.modules.impl.exploit.Blink;
import dev.ripple.mod.modules.impl.player.SpeedMine;
import dev.ripple.mod.modules.impl.render.ExplosionSpawn;
import dev.ripple.mod.modules.settings.SwingSide;
import dev.ripple.mod.modules.settings.impl.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import static dev.ripple.api.utils.world.BlockUtil.hasCrystal;

public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;
    public static BlockPos crystalPos;
    public final Timer lastBreakTimer = new Timer();
    private final Timer placeTimer = new Timer(), noPosTimer = new Timer(), switchTimer = new Timer(), calcDelay = new Timer();

    /* ------------------------------AutoCrystal------------------------------ */

    private final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
    //General
    private final BooleanSetting preferAnchor = add(new BooleanSetting("PreferAnchor", true, () -> page.is(Page.General)));
    private final BooleanSetting breakOnlyHasCrystal = add(new BooleanSetting("OnlyHold", true, () -> page.is(Page.General)));
    private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.All, () -> page.is(Page.General)));
    private final BooleanSetting eatingPause = add(new BooleanSetting("EatingPause", true, () -> page.is(Page.General)).setParent());
    private final BooleanSetting sameHand = add(new BooleanSetting("SameHand", true, () -> page.is(Page.General) && eatingPause.isOpen()));
    private final SliderSetting switchCooldown = add(new SliderSetting("SwitchPause", 100, 0, 1000, () -> page.is(Page.General)).setSuffix("ms"));
    private final SliderSetting targetRange = add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, () -> page.is(Page.General)).setSuffix("m"));
    private final SliderSetting updateDelay = add(new SliderSetting("UpdateDelay", 50, 0, 1000, () -> page.is(Page.General)).setSuffix("ms"));
    //Rotate
    private final BooleanSetting rotate = add(new BooleanSetting("Rotate", true, () -> page.is(Page.Rotation)).setParent());
    private final BooleanSetting onBreak = add(new BooleanSetting("OnBreak", false, () -> rotate.isOpen() && page.is(Page.Rotation)));
    private final SliderSetting yOffset = add(new SliderSetting("YOffset", 0.05, 0, 1, 0.01, () -> rotate.isOpen() && onBreak.getValue() && page.is(Page.Rotation)));
    private final BooleanSetting yawStep = add(new BooleanSetting("YawStep", false, () -> rotate.isOpen() && page.is(Page.Rotation)));
    private final SliderSetting steps = add(new SliderSetting("Steps", 0.05, 0, 1, 0.01, () -> rotate.isOpen() && yawStep.getValue() && page.is(Page.Rotation)));
    private final BooleanSetting checkFov = add(new BooleanSetting("OnlyLooking", true, () -> rotate.isOpen() && yawStep.getValue() && page.is(Page.Rotation)));
    private final SliderSetting fov = add(new SliderSetting("Fov", 30, 0, 50, () -> rotate.isOpen() && yawStep.getValue() && checkFov.getValue() && page.is(Page.Rotation)));
    private final SliderSetting priority = add(new SliderSetting("Priority", 10,0 ,100, () -> rotate.isOpen() && yawStep.getValue() && page.is(Page.Rotation)));
    //Place
    private final SliderSetting autoMinDamage = add(new SliderSetting("PistonMin", 5.0, 0.0, 36.0, () -> page.is(Page.Interact)).setSuffix("dmg"));
    private final SliderSetting minDamage = add(new SliderSetting("Min", 5.0, 0.0, 36.0, () -> page.is(Page.Interact)).setSuffix("dmg"));
    private final SliderSetting maxSelf = add(new SliderSetting("Self", 12.0, 0.0, 36.0, () -> page.is(Page.Interact)).setSuffix("dmg"));
    private final SliderSetting range = add(new SliderSetting("Range", 5.0, 0.0, 6, () -> page.is(Page.Interact)).setSuffix("m"));
    private final SliderSetting wallRange = add(new SliderSetting("WallRange", 5.0, 0.0, 6.0, () -> page.is(Page.Interact)).setSuffix("m"));
    private final SliderSetting noSuicide = add(new SliderSetting("NoSuicide", 3.0, 0.0, 10.0, () -> page.is(Page.Interact)).setSuffix("hp"));
    private final BooleanSetting smart = add(new BooleanSetting("Smart", true, () -> page.is(Page.Interact)));
    private final BooleanSetting place = add(new BooleanSetting("Place", true, () -> page.is(Page.Interact)).setParent());
    private final SliderSetting placeDelay = add(new SliderSetting("PlaceDelay", 300, 0, 1000, () -> page.is(Page.Interact) && place.isOpen()).setSuffix("ms"));
    private final EnumSetting<SwapMode> autoSwap = add(new EnumSetting<>("AutoSwap", SwapMode.Off, () -> page.is(Page.Interact) && place.isOpen()));
    private final BooleanSetting afterBreak = add(new BooleanSetting("AfterBreak", true, () -> page.is(Page.Interact) && place.isOpen()));
    private final BooleanSetting breakSetting = add(new BooleanSetting("Break", true, () -> page.is(Page.Interact)).setParent());
    private final SliderSetting breakDelay = add(new SliderSetting("BreakDelay", 300, 0, 1000, () -> page.is(Page.Interact) && breakSetting.isOpen()).setSuffix("ms"));
    private final SliderSetting minAge = add(new SliderSetting("MinAge", 0, 0, 20, () -> page.is(Page.Interact) && breakSetting.isOpen()).setSuffix("tick"));
    private final BooleanSetting breakRemove = add(new BooleanSetting("Remove", false, () -> page.is(Page.Interact) && breakSetting.isOpen()));
    private final BooleanSetting onlyTick = add(new BooleanSetting("OnlyTick", true, () -> page.is(Page.Interact)));
    //Render
    private final ColorSetting text = add(new ColorSetting("Text", new Color(-1), () -> page.is(Page.Render)).injectBoolean(true));
    private final BooleanSetting render = add(new BooleanSetting("Render", true, () -> page.is(Page.Render)));
    private final BooleanSetting sync = add(new BooleanSetting("Sync", true, () -> page.is(Page.Render) && render.getValue()));
    private final BooleanSetting shrink = add(new BooleanSetting("Shrink", true, () -> page.is(Page.Render) && render.getValue()));
    private final ColorSetting box = add(new ColorSetting("Box", new Color(255, 255, 255, 255), () -> page.is(Page.Render) && render.getValue()).injectBoolean(true));
    private final SliderSetting lineWidth = add(new SliderSetting("LineWidth", 1.5d, 0.01d, 3d, 0.01, () -> page.is(Page.Render) && render.getValue()));
    private final ColorSetting fill = add(new ColorSetting("Fill", new Color(255, 255, 255, 100), () -> page.is(Page.Render) && render.getValue()).injectBoolean(true));
    private final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.75, 0.01, 1, 0.01, () -> page.is(Page.Render) && render.getValue()));
    private final SliderSetting startFadeTime = add(new SliderSetting("StartFade", 0.3d, 0d, 2d, 0.01, () -> page.is(Page.Render) && render.getValue()).setSuffix("s"));
    private final SliderSetting fadeSpeed = add(new SliderSetting("FadeSpeed", 0.2d, 0.01d, 1d, 0.01, () -> page.is(Page.Render) && render.getValue()));

    private final EnumSetting<TargetESP> mode = add(new EnumSetting<>("TargetESP", TargetESP.Jello, () -> page.is(Page.Render)));
    private final ColorSetting color = add(new ColorSetting("TargetColor", new Color(255, 255, 255, 50), () -> page.is(Page.Render)));
    private final ColorSetting hitColor = add(new ColorSetting("HitColor", new Color(255, 255, 255, 150), () -> page.is(Page.Render)));
    public final SliderSetting animationTime = add(new SliderSetting("AnimationTime", 200, 0, 2000, 1, () -> page.is(Page.Render) && mode.is(TargetESP.Box)));
    public final EnumSetting<Easing> ease = add(new EnumSetting<>("Ease", Easing.CubicInOut, () -> page.is(Page.Render) && mode.is(TargetESP.Box)));
    //Calc
    private final BooleanSetting thread = add(new BooleanSetting("Thread", true, () -> page.is(Page.Calc)));
    private final BooleanSetting doCrystal = add(new BooleanSetting("ThreadInteract", false, () -> page.is(Page.Calc)));
    private final BooleanSetting lite = add(new BooleanSetting("LessCPU", false, () -> page.is(Page.Calc)));
    private final SliderSetting predictTicks = add(new SliderSetting("Predict", 4, 0, 10, () -> page.is(Page.Calc)).setSuffix("ticks"));
    private final BooleanSetting terrainIgnore = add(new BooleanSetting("TerrainIgnore", true, () -> page.is(Page.Calc)));
    //Misc
    private final BooleanSetting ignoreMine = add(new BooleanSetting("IgnoreMine", true, () -> page.is(Page.Misc)).setParent());
    private final SliderSetting constantProgress = add(new SliderSetting("Progress", 90.0, 0.0, 100.0, () -> page.is(Page.Misc) && ignoreMine.isOpen()).setSuffix("%"));
    private final BooleanSetting antiSurround = add(new BooleanSetting("AntiSurround", false, () -> page.is(Page.Misc)).setParent());
    private final SliderSetting antiSurroundMax = add(new SliderSetting("WhenLower", 5.0, 0.0, 36.0, () -> page.is(Page.Misc) && antiSurround.isOpen()).setSuffix("dmg"));
    private final BooleanSetting slowPlace = add(new BooleanSetting("Timeout", true, () -> page.is(Page.Misc)).setParent());
    private final SliderSetting slowDelay = add(new SliderSetting("TimeoutDelay", 600, 0, 2000, () -> page.is(Page.Misc) && slowPlace.isOpen()).setSuffix("ms"));
    private final SliderSetting slowMinDamage = add(new SliderSetting("TimeoutMin", 1.5, 0.0, 36.0, () -> page.is(Page.Misc) && slowPlace.isOpen()).setSuffix("dmg"));
    private final BooleanSetting forcePlace = add(new BooleanSetting("ForcePlace", true, () -> page.is(Page.Misc)).setParent());
    private final BindSetting forceKey = add(new BindSetting("ForceKey", GLFW.GLFW_KEY_LEFT_SHIFT, () -> page.is(Page.Misc) && forcePlace.isOpen()));
    private final SliderSetting forceMaxHealth = add(new SliderSetting("LowerThan", 7, 0, 36, () -> page.is(Page.Misc) && forcePlace.isOpen()).setSuffix("health"));
    private final SliderSetting forceMin = add(new SliderSetting("ForceMin", 1.5, 0.0, 36.0, () -> page.is(Page.Misc) && forcePlace.isOpen()).setSuffix("dmg"));
    private final BooleanSetting armorBreaker = add(new BooleanSetting("ArmorBreaker", true, () -> page.is(Page.Misc)).setParent());
    private final SliderSetting maxDurable = add(new SliderSetting("MaxDurable", 8, 0, 100, () -> page.is(Page.Misc) && armorBreaker.isOpen()).setSuffix("%"));
    private final SliderSetting armorBreakerDamage = add(new SliderSetting("BreakerMin", 3.0, 0.0, 36.0, () -> page.is(Page.Misc) && armorBreaker.isOpen()).setSuffix("dmg"));
    private final SliderSetting hurtTime = add(new SliderSetting("HurtTime", 10, 0, 10, 1, () -> page.is(Page.Misc)));
    private final SliderSetting waitHurt = add(new SliderSetting("WaitHurt", 10, 0, 10, 1, () -> page.is(Page.Misc)));
    private final SliderSetting syncTimeout = add(new SliderSetting("WaitTimeOut", 500, 0, 2000, 10, () -> page.is(Page.Misc)));
    private final BooleanSetting forceWeb = add(new BooleanSetting("ForceWeb", true, () -> page.is(Page.Misc)).setParent());
    public final BooleanSetting airPlace = add(new BooleanSetting("AirPlace", false, () -> page.is(Page.Misc) && forceWeb.isOpen()));
    public final BooleanSetting replace = add(new BooleanSetting("Replace", false, () -> page.is(Page.Misc) && forceWeb.isOpen()));
    //Info
    private final BooleanSetting target = add(new BooleanSetting("TargetName", true, () -> page.is(Page.Info)));
    private final BooleanSetting damage = add(new BooleanSetting("DamageInfo", true, () -> page.is(Page.Info)));
    public PlayerEntity displayTarget;
    private final Animation animation = new Animation();
    public float breakDamage, tempDamage, lastDamage;
    public Vec3d directionVec = null;
    double currentFade = 0;
    private BlockPos tempPos, breakPos, syncPos;
    private Vec3d placeVec3d, curVec3d;

    /* ------------------------------BasePlace------------------------------ */

    public static BlockPos crystalPosB;

    private final Timer placeTimerB = new Timer();
    private final Timer delayTimerB = new Timer();
    //General
    private final BooleanSetting enable = add(new BooleanSetting("Enable", true, () -> page.is(Page.BasePlace)));
    private final BooleanSetting inventoryB = add(new BooleanSetting("InventorySwap", true, () -> page.is(Page.BasePlace)));
    private final BooleanSetting detectMiningB = add(new BooleanSetting("DetectMining", false, () -> page.is(Page.BasePlace)));
    private final BooleanSetting breakOnlyHasCrystalB = add(new BooleanSetting("OnlyCrystalFound", false, () -> page.is(Page.BasePlace)));
    private final BooleanSetting eatingPauseB = add(new BooleanSetting("BaseEatingPause", true, () -> page.is(Page.BasePlace)).setParent());
    private final BooleanSetting sameHandB = add(new BooleanSetting("BaseSameHand", true, () -> page.is(Page.BasePlace) && eatingPauseB.isOpen()));
    //Place
    private final SliderSetting rangeB = add(new SliderSetting("BaseRange", 5.0, 0.0, 6, () -> page.is(Page.BasePlace)).setSuffix("m"));
    private final SliderSetting placeRangeB = add(new SliderSetting("PlaceRange", 5.0, 0.0, 6, () -> page.is(Page.BasePlace)).setSuffix("m"));
    private final SliderSetting placeDelayB = add(new SliderSetting("BaseDelay", 300, 0, 1000, () -> page.is(Page.BasePlace)).setSuffix("ms"));
    public PlayerEntity displayTargetB;
    public float breakDamageB, tempDamageB, lastDamageB;
    private BlockPos tempPosB;

    private enum Page {
        General, Interact, Misc, Rotation, Calc, Render, Info, BasePlace
    }

    private enum SwapMode {
        Off, Normal, Silent, Inventory
    }

    public enum TargetESP {
        Box,
        Jello,
        None
    }

    public AutoCrystal() {
        super("AutoCrystal", Category.Combat);
        setChinese("自动水晶");
        INSTANCE = this;
        Ripple.EVENT_BUS.subscribe(new CrystalRender());
    }

    public static boolean canSee(Vec3d from, Vec3d to) {
        HitResult result = mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    DecimalFormat df = new DecimalFormat("0.0");
    @Override
    public String getInfo() {
        if (displayTarget != null && lastDamage > 0) {
            if (target.getValue() && damage.getValue()) return displayTarget.getName().getString() + ", " + df.format(lastDamage);
            else if (target.getValue()) return displayTarget.getName().getString();
            else return df.format(lastDamage);
            //return df.format(lastDamage);
        }
        return null;
    }

    @Override
    public void onDisable() {
        crystalPosB = null;
        tempPos = null;
        crystalPos = null;
        tempPosB = null;
    }

    @Override
    public void onEnable() {
        crystalPos = null;
        tempPos = null;
        breakPos = null;
        displayTarget = null;
        syncTimer.reset();
        lastBreakTimer.reset();
    }

    @Override
    public void onThread() {
        if (thread.getValue()) {
            updateCrystalPos();
            if (enable.getValue()) {
                updateCrystalPosB();
            }
        }
    }

    @Override
    public void onUpdate() {
        if (!thread.getValue()) {
            updateCrystalPos();
            if (enable.getValue()) updateCrystalPosB();
        }
        doInteract();
        if (enable.getValue()) doBaseInteract();
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (!thread.getValue()) updateCrystalPos();
        if (!thread.getValue() && enable.getValue()) updateCrystalPosB();
        if (!onlyTick.getValue()) doInteract();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack) {
        if (!thread.getValue()) updateCrystalPos();
        if (!thread.getValue() && enable.getValue()) updateCrystalPosB();
        if (!onlyTick.getValue()) doInteract();
        if (displayTarget != null && !noPosTimer.passedMs(500)) {
            doRender(matrixStack, mc.getRenderTickCounter().getTickDelta(true), displayTarget, mode.getValue());
        }
    }

    public void doRender(MatrixStack matrixStack, float partialTicks, Entity entity, TargetESP mode) {
        switch (mode) {
            case Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity) entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0, 0.1, 0), ColorUtil.fadeColor(color.getValue(), hitColor.getValue(), animation.get(0, animationTime.getValueInt(), ease.getValue())), false, true);
            case Jello -> JelloUtil.drawJello(matrixStack, entity, color.getValue());
        }
    }

    private void doInteract() {
        if (shouldReturn()) {
            return;
        }
        if (breakPos != null) {
            doBreak(breakPos);
            breakPos = null;
        }
        if (crystalPos != null) {
            doCrystal(crystalPos);
        }
    }

    @EventHandler
    public void onRotate(LookAtEvent event) {
        if (rotate.getValue() && yawStep.getValue() && directionVec != null && !noPosTimer.passed(1000)) {
            event.setTarget(directionVec, steps.getValueFloat(), priority.getValueFloat());
        }
        if (directionVecB != null && rotate.getValue() && yawStep.getValue() && enable.getValue()) {
            event.setTarget(directionVecB, steps.getValueFloat(), priority.getValueFloat());
        }
    }

    @EventHandler(priority = -199)
    public void onPacketSend(PacketEvent.Send event) {
        if (event.isCancelled()) return;
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer.reset();
        }
    }

    private void updateCrystalPos() {
        getCrystalPos();
        lastDamage = tempDamage;
        crystalPos = tempPos;
    }

    private boolean shouldReturn() {
        if ((eatingPause.getValue() && !sameHand.getValue() && mc.player.isUsingItem()) || (eatingPause.getValue() && sameHand.getValue() && mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND) || Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            lastBreakTimer.reset();
            return true;
        }
        if (preferAnchor.getValue() && AutoAnchor.INSTANCE.currentPos != null) {
            lastBreakTimer.reset();
            return true;
        }
        return false;
    }

    private void getCrystalPos() {
        if (nullCheck()) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        if (!calcDelay.passedMs((long) updateDelay.getValue())) return;
        if (breakOnlyHasCrystal.getValue() && !mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            lastBreakTimer.reset();
            tempPos = null;
            return;
        }
        boolean shouldReturn = shouldReturn();
        calcDelay.reset();
        breakPos = null;
        breakDamage = 0;
        tempPos = null;
        tempDamage = 0f;
        ArrayList<PlayerAndPredict> list = new ArrayList<>();
        for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getValueFloat())) {
            if (target.hurtTime <= hurtTime.getValueInt()) {
                list.add(new PlayerAndPredict(target));
            }
        }
        PlayerAndPredict self = new PlayerAndPredict(mc.player);
        if (list.isEmpty()) {
            lastBreakTimer.reset();
        } else {
            for (BlockPos pos : BlockUtil.getSphere((float) range.getValue() + 1)) {
                if (behindWall(pos)) continue;
                if (mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > range.getValue()) {
                    continue;
                }
                if (!canTouch(pos.down())) continue;
                if (!canPlaceCrystal(pos, true, false)) continue;
                for (PlayerAndPredict pap : list) {
                    if (lite.getValue() && liteCheck(pos.toCenterPos().add(0, -0.5, 0), pap.predict.getPos())) {
                        continue;
                    }
                    float damage = calculateDamage(pos, pap.player, pap.predict);
                    if (tempPos == null || damage > tempDamage) {
                        float selfDamage = calculateDamage(pos, self.player, self.predict);
                        if (selfDamage > maxSelf.getValue()) continue;
                        if (noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())
                            continue;
                        if (damage < EntityUtil.getHealth(pap.player)) {
                            if (damage < getDamage(pap.player)) continue;
                            if (smart.getValue()) {
                                if (getDamage(pap.player) == forceMin.getValue()) {
                                    if (damage < selfDamage - 2.5) {
                                        continue;
                                    }
                                } else {
                                    if (damage < selfDamage) {
                                        continue;
                                    }
                                }
                            }
                        }
                        displayTarget = pap.player;
                        tempPos = pos;
                        tempDamage = damage;
                    }
                }
            }
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity crystal) {
                    if (!mc.player.canSee(crystal) && mc.player.getEyePos().distanceTo(crystal.getPos()) > wallRange.getValue())
                        continue;
                    if (mc.player.getEyePos().distanceTo(crystal.getPos()) > range.getValue()) {
                        continue;
                    }
                    for (PlayerAndPredict pap : list) {
                        float damage = calculateDamage(crystal.getPos(), pap.player, pap.predict);
                        if (breakPos == null || damage > breakDamage) {
                            float selfDamage = calculateDamage(crystal.getPos(), self.player, self.predict);
                            if (selfDamage > maxSelf.getValue()) continue;
                            if (noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())
                                continue;
                            if (damage < EntityUtil.getHealth(pap.player)) {
                                if (damage < getDamage(pap.player)) continue;
                                if (smart.getValue()) {
                                    if (getDamage(pap.player) == forceMin.getValue()) {
                                        if (damage < selfDamage - 2.5) {
                                            continue;
                                        }
                                    } else {
                                        if (damage < selfDamage) {
                                            continue;
                                        }
                                    }
                                }
                            }
                            breakPos = new BlockPosX(crystal.getPos());
                            if (damage > tempDamage) {
                                displayTarget = pap.player;
                                //tempDamage = damage;
                            }
                        }
                    }
                }
            }
            if (doCrystal.getValue() && breakPos != null && !shouldReturn) {
                doBreak(breakPos);
                breakPos = null;
            }
            if (antiSurround.getValue() && SpeedMine.getBreakPos() != null && SpeedMine.progress >= 0.9 && !BlockUtil.hasEntity(SpeedMine.getBreakPos(), false)) {
                if (tempDamage <= antiSurroundMax.getValueFloat()) {
                    for (PlayerAndPredict pap : list) {
                        for (Direction i : Direction.values()) {
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos = new BlockPosX(pap.player.getPos().add(0, 0.5, 0)).offset(i);
                            if (offsetPos.equals(SpeedMine.getBreakPos())) {
                                if (canPlaceCrystal(offsetPos.offset(i), false, false)) {
                                    float selfDamage = calculateDamage(offsetPos.offset(i), self.player, self.predict);
                                    if (selfDamage < maxSelf.getValue() && !(noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())) {
                                        tempPos = offsetPos.offset(i);
                                        if (doCrystal.getValue() && tempPos != null && !shouldReturn) {
                                            doCrystal(tempPos);
                                        }
                                        return;
                                    }
                                }
                                for (Direction ii : Direction.values()) {
                                    if (ii == Direction.DOWN || ii == i) continue;
                                    if (canPlaceCrystal(offsetPos.offset(ii), false, false)) {
                                        float selfDamage = calculateDamage(offsetPos.offset(ii), self.player, self.predict);
                                        if (selfDamage < maxSelf.getValue() && !(noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())) {
                                            tempPos = offsetPos.offset(ii);
                                            if (doCrystal.getValue() && tempPos != null && !shouldReturn) {
                                                doCrystal(tempPos);
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (doCrystal.getValue() && tempPos != null && !shouldReturn) {
            doCrystal(tempPos);
        }
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        BlockPos boost2 = boost.up();

        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
                && BlockUtil.getClickSideStrict(obsPos) != null
                && noEntityBlockCrystal(boost, ignoreCrystal, ignoreItem)
                && noEntityBlockCrystal(boost2, ignoreCrystal, ignoreItem)
                && (mc.world.isAir(boost) || hasCrystal(boost) && getBlock(boost) == Blocks.FIRE)
                && (!ClientSetting.INSTANCE.lowVersion.getValue() || mc.world.isAir(boost2));
    }

    private boolean liteCheck(Vec3d from, Vec3d to) {
        return !canSee(from, to) && !canSee(from, to.add(0, 1.8, 0));
    }

    private boolean noEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue())
                continue;
            if (entity instanceof EndCrystalEntity) {
                if (!ignoreCrystal) return false;
                if (mc.player.canSee(entity) || mc.player.getEyePos().distanceTo(entity.getPos()) <= wallRange.getValue()) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    public boolean behindWall(BlockPos pos) {
        Vec3d testVec;
        /*if (CombatSetting.INSTANCE.lowVersion.getValue()) {
            testVec = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        } else {
            testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        }*/
        testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        HitResult result = mc.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        if (result == null || result.getType() == HitResult.Type.MISS) return false;
        return mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > wallRange.getValue();
    }

    private boolean canTouch(BlockPos pos) {
        Direction side = BlockUtil.getClickSideStrict(pos);
        return side != null && pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5)).distanceTo(mc.player.getEyePos()) <= range.getValue();
    }

    private void doCrystal(BlockPos pos) {
        if (canPlaceCrystal(pos, false, false)) {
            doPlace(pos);
        } else {
            doBreak(pos);
        }
    }

    public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
        return calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), player, predict);
    }

    public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
        if (ignoreMine.getValue() && SpeedMine.getBreakPos() != null) {
            if (mc.player.getEyePos().distanceTo(SpeedMine.getBreakPos().toCenterPos()) <= SpeedMine.INSTANCE.range.getValue()) {
                if (SpeedMine.progress >= constantProgress.getValue() / 100) {
                    CombatUtil.modifyPos = SpeedMine.getBreakPos();
                    CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                }
            }
        }
        if (terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        float damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
        CombatUtil.modifyPos = null;
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    private double getDamage(PlayerEntity target) {
        if (!SpeedMine.INSTANCE.obsidian.isPressed() && slowPlace.getValue() && lastBreakTimer.passedMs((long) slowDelay.getValue()) && !PistonCrystal.INSTANCE.isOn()) {
            return slowMinDamage.getValue();
        }
        if (forcePlace.getValue() && (EntityUtil.getHealth(target) <= forceMaxHealth.getValue() || forceKey.isPressed()) && !SpeedMine.INSTANCE.obsidian.isPressed() && !PistonCrystal.INSTANCE.isOn()) {
            return forceMin.getValue();
        }
        if (armorBreaker.getValue()) {
            DefaultedList<ItemStack> armors = target.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) > maxDurable.getValue()) continue;
                return armorBreakerDamage.getValue();
            }
        }
        if (PistonCrystal.INSTANCE.isOn()) {
            return autoMinDamage.getValueFloat();
        }
        return minDamage.getValue();
    }

    public boolean findCrystal() {
        if (autoSwap.getValue() == SwapMode.Off) return false;
        return getCrystal() != -1;
    }

    private final Timer syncTimer = new Timer();

    private void doBreak(BlockPos pos) {
        noPosTimer.reset();
        if (!breakSetting.getValue()) return;
        if (displayTarget != null && displayTarget.hurtTime > waitHurt.getValueInt() && !syncTimer.passed(syncTimeout.getValue())) {
            return;
        }
        lastBreakTimer.reset();
        if (!switchTimer.passedMs((long) switchCooldown.getValue())) {
            return;
        }
        syncTimer.reset();
        for (EndCrystalEntity entity : BlockUtil.getEndCrystals(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1))) {
            if (entity.age < minAge.getValueInt()) continue;
            if (rotate.getValue() && onBreak.getValue()) {
                if (!faceVector(entity.getPos().add(0, yOffset.getValue(), 0))) return;
            }
            if (!CombatUtil.breakTimer.passedMs((long) breakDelay.getValue())) return;
            animation.to = 1;
            animation.from = 1;
            CombatUtil.breakTimer.reset();
            syncPos = pos;
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            mc.player.resetLastAttackedTicks();
            EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
            if (breakRemove.getValue()) {
                mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
            }
            if (crystalPos != null && displayTarget != null && lastDamage >= getDamage(displayTarget) && afterBreak.getValue()) {
                if (!yawStep.getValue() || !checkFov.getValue() || Ripple.ROTATION.inFov(entity.getPos(), fov.getValueFloat())) {
                    doPlace(crystalPos);
                }
            }
            if (forceWeb.getValue() && AutoWeb.INSTANCE.isOn()) {
                AutoWeb.force = true;
            }
            if (rotate.getValue() && !yawStep.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
                Ripple.ROTATION.snapBack();
            }
           return;
        }
    }

    private void doPlace(BlockPos pos) {
        noPosTimer.reset();
        if (!place.getValue()) return;
        if (!mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            return;
        }
        if (!canTouch(pos.down())) {
            return;
        }
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5, facing.getVector().getY() * 0.5, facing.getVector().getZ() * 0.5);
        if (facing != Direction.UP && facing != Direction.DOWN) {
            vec = vec.add(0, 0.45, 0);
        }
        if (rotate.getValue()) {
            if (!faceVector(vec)) return;
        }
        if (!placeTimer.passedMs((long) placeDelay.getValue())) return;
        if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
            placeTimer.reset();
            syncPos = pos;
            placeCrystal(pos);
        } else {
            placeTimer.reset();
            syncPos = pos;
            int old = mc.player.getInventory().selectedSlot;
            int crystal = getCrystal();
            if (crystal == -1) return;
            doSwap(crystal);
            placeCrystal(pos);
            if (autoSwap.getValue() == SwapMode.Silent) {
                doSwap(old);
            } else if (autoSwap.getValue() == SwapMode.Inventory) {
                doSwap(crystal);
                EntityUtil.syncInventory();
            }
        }
    }

    private void doSwap(int slot) {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            InventoryUtil.switchToSlot(slot);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        }
    }

    private int getCrystal() {
        if (autoSwap.getValue() == SwapMode.Silent || autoSwap.getValue() == SwapMode.Normal) {
            return InventoryUtil.findItem(Items.END_CRYSTAL);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
        }
        return -1;
    }

    private void placeCrystal(BlockPos pos) {
        ExplosionSpawn.INSTANCE.add(pos);
        //PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
        boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        BlockUtil.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, swingMode.getValue());
    }

    private boolean faceVector(Vec3d directionVec) {
        if (!yawStep.getValue()) {
            Ripple.ROTATION.lookAt(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            if (Ripple.ROTATION.inFov(directionVec, fov.getValueFloat())) {
                return true;
            }
        }
        return !checkFov.getValue();
    }

    private class PlayerAndPredict {
        final PlayerEntity player;
        final PlayerEntity predict;

        private PlayerAndPredict(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValueFloat() > 0) {
                predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }

                    @Override
                    public boolean isOnGround() {
                        return player.isOnGround();
                    }
                };
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, predictTicks.getValueInt(), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : new ArrayList<>(player.getStatusEffects())) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }

    private class CrystalRender {
        @EventHandler
        public void onRender3D(Render3DEvent event) {
            BlockPos cpos = sync.getValue() && crystalPos != null ? syncPos : crystalPos;
            if (cpos != null) {
                placeVec3d = cpos.down().toCenterPos();
            }
            if (placeVec3d == null) {
                return;
            }
            if (fadeSpeed.getValue() >= 1) {
                currentFade = noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5;
            } else {
                currentFade = AnimateUtil.animate(currentFade, noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5, fadeSpeed.getValue() / 10);
            }
            if (currentFade == 0) {
                curVec3d = null;
                return;
            }
            if (curVec3d == null || sliderSpeed.getValue() >= 1) {
                curVec3d = placeVec3d;
            } else {
                curVec3d = new Vec3d(AnimateUtil.animate(curVec3d.x, placeVec3d.x, sliderSpeed.getValue() / 10), AnimateUtil.animate(curVec3d.y, placeVec3d.y, sliderSpeed.getValue() / 10), AnimateUtil.animate(curVec3d.z, placeVec3d.z, sliderSpeed.getValue() / 10));
            }
            if (render.getValue()) {
                Box cbox = new Box(curVec3d, curVec3d);
                if (shrink.getValue()) {
                    cbox = cbox.expand(currentFade);
                } else {
                    cbox = cbox.expand(0.5);
                }
                MatrixStack matrixStack = event.getMatrixStack();
                if (fill.booleanValue) {
                    Render3DUtil.drawFill(matrixStack, cbox, ColorUtil.injectAlpha(fill.getValue(), (int) (fill.getValue().getAlpha() * currentFade * 2D)));
                }
                if (box.booleanValue) {
                    Render3DUtil.drawBox(matrixStack, cbox, ColorUtil.injectAlpha(box.getValue(), (int) (box.getValue().getAlpha() * currentFade * 2D)), lineWidth.getValueFloat());
                }
            }
            if (text.booleanValue && lastDamage > 0) {
                if (!noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000))) Render3DUtil.drawText3D(df.format(lastDamage), curVec3d, text.getValue());
            }
        }
    }

    public Vec3d directionVecB = null;

    private boolean faceVectorB(Vec3d directionVecB) {
        if (!yawStep.getValue()) {
            Ripple.ROTATION.lookAt(directionVecB);
            return true;
        } else {
            this.directionVecB = directionVecB;
            if (Ripple.ROTATION.inFov(directionVecB, fov.getValueFloat())) {
                return true;
            }
        }
        return !checkFov.getValue();
    }

    private void doBaseInteract() {
        directionVecB = null;
        if (crystalPosB != null) {
            doPlaceB(crystalPosB);
        }
    }

    private void updateCrystalPosB() {
        updateB();
        lastDamageB = tempDamageB;
        crystalPosB = tempPosB;
    }

    private void updateB() {
        if (nullCheck()) return;
        if (!delayTimerB.passedMs((long) updateDelay.getValue())) return;
        if ((eatingPauseB.getValue() && mc.player.isUsingItem() && !sameHandB.getValue()) || (eatingPauseB.getValue() && mc.player.isUsingItem() && sameHandB.getValue() && mc.player.getActiveHand() == Hand.MAIN_HAND)) {
            tempPosB = null;
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            tempPosB = null;
            return;
        }
        if (breakOnlyHasCrystalB.getValue() && !mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !AutoCrystal.INSTANCE.findCrystal()) {
            tempPosB = null;
            return;
        }
        delayTimerB.reset();
        breakDamageB = 0;
        tempPosB = null;
        tempDamageB = 0f;
        ArrayList<PlayerAndPredictB> list = new ArrayList<>();
        for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getValueFloat())) {
            if (target.hurtTime <= hurtTime.getValueInt()) {
                list.add(new PlayerAndPredictB(target));
            }
        }
        PlayerAndPredictB self = new PlayerAndPredictB(mc.player);
        if (!list.isEmpty()) {
            for (BlockPos pos : BlockUtil.getSphere((float) rangeB.getValue() + 1)) {
                CombatUtil.modifyPos = null;
                if (mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > rangeB.getValue()) {
                    continue;
                }
                if (!canPlaceCrystalB(pos, true, false)) continue;
                CombatUtil.modifyPos = pos.down();
                CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
                if (behindWallB(pos)) continue;
                if (!canTouchB(pos.down())) continue;
                for (PlayerAndPredictB pap : list) {
                    if (pos.down().getY() > pap.player.getBlockY()) continue;
                    if (lite.getValue() && liteCheckB(pos.toCenterPos().add(0, -0.5, 0), pap.predict.getPos())) {
                        continue;
                    }
                    float damage = calcDamageB(pos, pap.player, pap.predict);
                    if (tempPosB == null || damage > tempDamageB) {
                        float selfDamage = calcDamageB(pos, self.player, self.predict);
                        if (selfDamage > maxSelf.getValue()) continue;
                        if (noSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - noSuicide.getValue())
                            continue;
                        if (damage < EntityUtil.getHealth(pap.player)) {
                            if (damage < getDamageB(pap.player)) continue;
                            if (smart.getValue()) {
                                if (getDamageB(pap.player) == forceMin.getValue()) {
                                    if (damage < selfDamage - 2.5) {
                                        continue;
                                    }
                                } else {
                                    if (damage < selfDamage) {
                                        continue;
                                    }
                                }
                            }
                        }
                        displayTargetB = pap.player;
                        tempPosB = pos.down();
                        tempDamageB = damage;
                    }
                }
            }
            CombatUtil.modifyPos = null;
            if (tempPosB != null) {
                if (!BlockUtil.canPlace(tempPosB, placeRangeB.getValue())) {
                    tempPosB = null;
                    tempDamageB = 0;
                }
            }
        }
        if (doCrystal.getValue() && tempPosB != null) {
            doPlaceB(tempPosB);
        }
    }

    public boolean canPlaceCrystalB(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        BlockPos boost2 = boost.up();

        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN || BlockUtil.canPlace(obsPos, placeRangeB.getValue())) && BlockUtil.getClickSideStrict(obsPos) != null && noEntityBlockCrystalB(boost, ignoreCrystal, ignoreItem) && noEntityBlockCrystalB(boost2, ignoreCrystal, ignoreItem) && (mc.world.isAir(boost) || hasCrystal(boost) && getBlock(boost) == Blocks.FIRE) && (!ClientSetting.INSTANCE.lowVersion.getValue() || mc.world.isAir(boost2));
    }

    private boolean liteCheckB(Vec3d from, Vec3d to) {
        return !canSee(from, to) && !canSee(from, to.add(0, 1.8, 0));
    }

    private boolean noEntityBlockCrystalB(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : BlockUtil.getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || entity instanceof ArmorStandEntity && AntiCheat.INSTANCE.obsMode.getValue())
                continue;
            if (entity instanceof EndCrystalEntity) {
                if (!ignoreCrystal) return false;
                if (mc.player.canSee(entity) || mc.player.getEyePos().distanceTo(entity.getPos()) <= wallRange.getValue()) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    private boolean behindWallB(BlockPos pos) {
        Vec3d testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        HitResult result = mc.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        if (result == null || result.getType() == HitResult.Type.MISS) return false;
        return mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0, -0.5, 0)) > wallRange.getValue();
    }

    private boolean canTouchB(BlockPos pos) {
        Direction side = BlockUtil.getClickSideStrict(pos);
        return side != null && pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5)).distanceTo(mc.player.getEyePos()) <= rangeB.getValue();
    }

    public float calcDamageB(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
        return calcDamageB(pos.down(), new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), player, predict);
    }

    public float calcDamageB(BlockPos obs, Vec3d pos, PlayerEntity player, PlayerEntity predict) {
        CombatUtil.modifyPos = obs;
        CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
        if (terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        float damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
        CombatUtil.modifyPos = null;
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    private double getDamageB(PlayerEntity target) {
        if (forcePlace.getValue() && (EntityUtil.getHealth(target) <= forceMaxHealth.getValue() || forceKey.isPressed()) && !SpeedMine.INSTANCE.obsidian.isPressed() && !PistonCrystal.INSTANCE.isOn()) {
            return forceMin.getValue();
        }
        if (armorBreaker.getValue()) {
            DefaultedList<ItemStack> armors = target.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.isEmpty()) continue;
                if (EntityUtil.getDamagePercent(armor) > maxDurable.getValue()) continue;
                return armorBreakerDamage.getValue();
            }
        }
        if (PistonCrystal.INSTANCE.isOn()) {
            return autoMinDamage.getValueFloat();
        }
        return minDamage.getValue();
    }


    private void doPlaceB(BlockPos pos) {
        if (!placeTimerB.passedMs((long) placeDelayB.getValue())) return;
        if (detectMiningB.getValue() && Ripple.BREAK.isMining(pos)) return;
        int block = getBlock();
        if (block == -1) return;
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) return;
        Vec3d directionVecB = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
        if (!BlockUtil.canPlace(pos, placeRangeB.getValue())) return;
        if (rotate.getValue()) {
            if (!faceVectorB(directionVecB)) return;
        }
        int old = mc.player.getInventory().selectedSlot;
        doSwapB(block);
        if (BlockUtil.airPlace()) {
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos, Direction.DOWN, false, Hand.MAIN_HAND);
        } else {
            BlockUtil.placedPos.add(pos);
            BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), false, Hand.MAIN_HAND);
        }
        if (inventoryB.getValue()) {
            doSwapB(block);
            EntityUtil.syncInventory();
        } else {
            doSwapB(old);
        }
        if (rotate.getValue() && !yawStep.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
            Ripple.ROTATION.snapBack();
        }
        placeTimerB.reset();
    }

    private void doSwapB(int slot) {
        if (inventoryB.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    public static Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    private int getBlock() {
        if (inventoryB.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }

    private class PlayerAndPredictB {
        final PlayerEntity player;
        final PlayerEntity predict;

        private PlayerAndPredictB(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValueFloat() > 0) {
                predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }

                    @Override
                    public boolean isOnGround() {
                        return player.isOnGround();
                    }
                };
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, predictTicks.getValueInt(), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : new ArrayList<>(player.getStatusEffects())) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }
}