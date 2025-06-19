package dev.ripple.mod.modules.impl.movement;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.eventbus.EventPriority;
import dev.ripple.api.events.impl.*;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.entity.MovementUtil;
import dev.ripple.api.utils.entity.PauseUtil;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.player.OffFirework;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class FakeFly extends Module {
    public static FakeFly INSTANCE;
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Control));
    public final BooleanSetting armor = add(new BooleanSetting("Armor", true));
    private final BooleanSetting stand = add(new BooleanSetting("Stand", false));
    private final SliderSetting timeout = add(new SliderSetting("Timeout", 0.5, 0.1, 1, 0.01));
    private final BooleanSetting key = add(new BooleanSetting("OnlyKeyRocket", false));
    private final BooleanSetting rotate = add(new BooleanSetting("Rotate", true, () -> mode.is(Mode.Control)));
    private final BooleanSetting rocket = add(new BooleanSetting("Firework", true));
    public final SliderSetting fireworkDelay = add(new SliderSetting("FireworkDelay", 1.2, 0.0, 5.0, 0.1, rocket::getValue));
    private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause", false, rocket::getValue).setParent());
    private final BooleanSetting same = add(new BooleanSetting("SameHand", false, () -> rocket.getValue() && usingPause.isOpen()));

    //private final BooleanSetting antiKick = add(new BooleanSetting("AntiKick", true).setParent());
    //private final SliderSetting delay = add(new SliderSetting("Delay", 5, 0.1, 20, 0.01, antiKick::isOpen).setSuffix("s"));
    //private final SliderSetting time = add(new SliderSetting("Time", 1.25, 0.1, 5, 0.01, antiKick::isOpen).setSuffix("s"));

    public final SliderSetting horizontalSpeed = add(new SliderSetting("HorizontalSpeed", 25, 0, 100, () -> mode.is(Mode.Control)));
    public final SliderSetting verticalSpeed = add(new SliderSetting("VerticalSpeed", 25, 0.0, 100, () -> mode.is(Mode.Control)));
    public final SliderSetting pitch = add(new SliderSetting("Pitch", 90, 0, 90, 0.1, () -> mode.is(Mode.Control) && rotate.getValue()));
    public final SliderSetting accelTime = add(new SliderSetting("AccelerationTime", 0, 0.01, 2.0, 0.01, () -> mode.is(Mode.Control)));

    public final BooleanSetting sprintToBoost = add(new BooleanSetting("SprintToBoost", true, () -> mode.is(Mode.Control)));
    public final SliderSetting sprintToBoostMaxSpeed = add(new SliderSetting("BoostMaxSpeed", 100.0, 50.0, 300.0, () -> mode.is(Mode.Control)));
    public final SliderSetting boostAccelTime = add(new SliderSetting("BoostAccelTime", 0.5, 0.01, 2.0, 0.01, () -> mode.is(Mode.Control)));
    private Vec3d lastMovement = Vec3d.ZERO;
    private Vec3d currentVelocity = Vec3d.ZERO;
    private long timeOfLastRubberband = 0L;
    private Vec3d lastRubberband = Vec3d.ZERO;
    public boolean fly = false;
    private final Timer instantFlyTimer = new Timer();
    private final Timer rocketTimer = new Timer();
    private final Timer spoofTimer = new Timer();

    public FakeFly() {
        super("FakeFly", Category.Movement);
        setChinese("虚假飞行");
        INSTANCE = this;
    }

    public enum Mode {
        Control, Legit
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        currentVelocity = mc.player.getVelocity();
        fly = false;
        if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
        mc.player.getAbilities().flying = false;
        spoofTimer.reset();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;
        InventoryUtil.silentSwapEquipChestplate();
        EntityUtil.syncInventory();
        if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
        mc.player.getAbilities().flying = false;
        sync();
    }

    private void sync() {
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        mc.player.setSneaking(false);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (mc.player.isOnGround()) fly = false;
        if (!mc.player.isFallFlying()) {
            if (!mc.player.isOnGround() && mc.player.getVelocity().getY() < 0D) {
                if (!instantFlyTimer.passedMs((long) (1000 * timeout.getValue()))) return;
                instantFlyTimer.reset();
                fly = true;
                //r();
            }
        } else {
            fly = true;
        }
    }

    /*
    private void r() {
        spoofTimer.reset();
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep((long) delay.getValueFloat() * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            pauseTimer.reset();
        });
    }

     */

    @EventHandler(priority = EventPriority.LOW)
    private void onRotate(RotateEvent event) {
        if (nullCheck()) return;
        if (!fly) return;
        if (mode.is(Mode.Legit)) return;
        if (!rotate.getValue()) return;
        event.setYaw(Sprint.getSprintYaw(mc.player.getYaw()));
        if (mc.options.jumpKey.isPressed()) {
            if (MovementUtil.isMoving()) event.setPitch(-(pitch.getValueFloat() / 2));
            else event.setPitch(-pitch.getValueFloat());
        } else if (mc.options.sneakKey.isPressed()) {
            if (MovementUtil.isMoving()) event.setPitch(pitch.getValueFloat() / 2);
            else event.setPitch(pitch.getValueFloat());
        }
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (event.isPre()) return;
        if (nullCheck()) return;
        if (!fly) return;
        /*
        if (spoofTimer.passedS(delay.getValueFloat()) && antiKick.getValue()) {
            if (!pauseTimer.passedS(time.getValueFloat())) return;
            else r();
        }

         */
        boolean isUsingFirework = getIsUsingFirework();
        if (isUsingFirework || InventoryUtil.findItemInventorySlot(Items.FIREWORK_ROCKET) != -1) {
            Vec3d desiredVelocity = new Vec3d(0.0D, 0.0D, 0.0D);
            double yaw = Math.toRadians(mc.player.getYaw());
            double pitch = Math.toRadians(mc.player.getPitch());
            Vec3d direction = (new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch))).normalize();
            if (mc.options.forwardKey.isPressed()) {
                desiredVelocity = desiredVelocity.add(direction.multiply(getHorizontalSpeed() / 20.0D, 0.0D, getHorizontalSpeed() / 20.0D));
            }

            if (mc.options.backKey.isPressed()) {
                desiredVelocity = desiredVelocity.add(direction.multiply(-getHorizontalSpeed() / 20.0D, 0.0D, -getHorizontalSpeed() / 20.0D));
            }

            if (mc.options.leftKey.isPressed()) {
                desiredVelocity = desiredVelocity.add(direction.multiply(getHorizontalSpeed() / 20.0D, 0.0D, getHorizontalSpeed() / 20.0D).rotateY(1.5707964F));
            }

            if (mc.options.rightKey.isPressed()) {
                desiredVelocity = desiredVelocity.add(direction.multiply(getHorizontalSpeed() / 20.0D, 0.0D, getHorizontalSpeed() / 20.0D).rotateY(-1.5707964F));
            }

            if (mc.options.jumpKey.isPressed()) {
                desiredVelocity = desiredVelocity.add(0.0D, verticalSpeed.getValueFloat() / 20.0D, 0.0D);
            }

            if (mc.options.sneakKey.isPressed()) {
                desiredVelocity = desiredVelocity.add(0.0D, -verticalSpeed.getValueFloat() / 20.0D, 0.0D);
            }

            currentVelocity = new Vec3d(mc.player.getVelocity().x, currentVelocity.y, mc.player.getVelocity().z);
            Vec3d velocityDifference = desiredVelocity.subtract(currentVelocity);
            double maxDelta = getHorizontalSpeed() / 20.0D / (getHorizontalAccelTime() * 20.0D);
            if (velocityDifference.lengthSquared() > maxDelta * maxDelta) {
                velocityDifference = velocityDifference.normalize().multiply(maxDelta);
            }

            currentVelocity = currentVelocity.add(velocityDifference);
            Box boundingBox = mc.player.getBoundingBox();
            double playerFeetY = boundingBox.minY;
            Box groundBox = new Box(boundingBox.minX, playerFeetY - 0.1D, boundingBox.minZ, boundingBox.maxX, playerFeetY, boundingBox.maxZ);

            for (BlockPos pos : BlockPos.iterate((int) Math.floor(groundBox.minX), (int) Math.floor(groundBox.minY), (int) Math.floor(groundBox.minZ), (int) Math.floor(groundBox.maxX), (int) Math.floor(groundBox.maxY), (int) Math.floor(groundBox.maxZ))) {
                BlockState blockState = mc.world.getBlockState(pos);
                if (blockState.isSolidBlock(mc.world, pos)) {
                    double blockTopY = (double) pos.getY() + 1.0D;
                    double distanceToBlock = playerFeetY - blockTopY;
                    if (distanceToBlock >= 0.0D && distanceToBlock < 0.1D && currentVelocity.y < 0.0D) {
                        currentVelocity = new Vec3d(currentVelocity.x, 0.1D, currentVelocity.z);
                    }
                }
            }

            if (armor.getValue()) {
                InventoryUtil.silentSwapEquipElytra();
                sync();
            }

            if (!mc.player.isFallFlying() || armor.getValue()) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }

            if (rocketTimer.passedS(fireworkDelay.getValueFloat()) && rocket.getValue()
                    && (MovementUtil.isMoving() || !key.getValue())) {
                if (!usingPause.getValue() || !PauseUtil.checkPause(same.getValue())) {
                    OffFirework.INSTANCE.off();
                    rocketTimer.reset();
                }
            }

            if (stand.getValue()) {
                mc.player.stopFallFlying();
                mc.player.setPose(EntityPose.STANDING);
            }

            if (armor.getValue()) {
                InventoryUtil.silentSwapEquipChestplate();
                sync();
            }
        }
    }

    @EventHandler
    private void onPlayerMove(MoveEvent event) {
        if (mode.is(Mode.Legit)) return;
        if (!fly) return;
        /*
        if (spoofTimer.passedS(delay.getValueFloat()) && antiKick.getValue()) {
            if (!pauseTimer.passedS(time.getValueFloat())) return;
            else r();
        }

         */
        if (getIsUsingFirework() || InventoryUtil.findItemInventorySlot(Items.FIREWORK_ROCKET) != -1) {
            if (lastMovement == null) {
                lastMovement = new Vec3d(event.getX(), event.getY(), event.getZ());
            }

            Vec3d newMovement = currentVelocity;
            mc.player.setVelocity(newMovement);
            event.setX(newMovement.x);
            event.setY(newMovement.y);
            event.setZ(newMovement.z);            
            lastMovement = newMovement;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!fly) return;
        /*
        if (spoofTimer.passedS(delay.getValueFloat()) && antiKick.getValue()) {
            if (!pauseTimer.passedS(time.getValueFloat())) return;
            else r();
        }

         */
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            if (packet.getFlags().contains(PositionFlag.X)) {
                currentVelocity = new Vec3d(packet.getX(), currentVelocity.y, currentVelocity.z);
            }

            if (packet.getFlags().contains(PositionFlag.Y)) {
                currentVelocity = new Vec3d(currentVelocity.x, packet.getY(), currentVelocity.z);
            }

            if (packet.getFlags().contains(PositionFlag.Z)) {
                currentVelocity = new Vec3d(currentVelocity.x, currentVelocity.y, packet.getZ());
            }

            if (!packet.getFlags().contains(PositionFlag.X) && !packet.getFlags().contains(PositionFlag.Y) && !packet.getFlags().contains(PositionFlag.Z)) {
                if (System.currentTimeMillis() - timeOfLastRubberband < 100L) {
                    currentVelocity = (new Vec3d(packet.getX(), packet.getY(), packet.getZ())).subtract(lastRubberband);
                }

                timeOfLastRubberband = System.currentTimeMillis();
                lastRubberband = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            }
        }

    }

    private boolean getIsUsingFirework() {
        boolean usingFirework = false;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof FireworkRocketEntity firework) {
                if (firework.getOwner() != null && firework.getOwner().equals(mc.player)) {
                    usingFirework = true;
                }
            }
        }

        return usingFirework;
    }

    private double getHorizontalSpeed() {
        if (mc.options.sprintKey.isPressed() && sprintToBoost.getValue()) {
            double horizontalVelocity = currentVelocity.horizontalLength();
            return Math.clamp(horizontalVelocity * 1.3D * 20.0D, horizontalSpeed.getValue(), sprintToBoostMaxSpeed.getValue());
        } else {
            return horizontalSpeed.getValue();
        }
    }

    private double getHorizontalAccelTime() {
        return currentVelocity.horizontalLength() > horizontalSpeed.getValue() ? boostAccelTime.getValue() : accelTime.getValue();
    }
}