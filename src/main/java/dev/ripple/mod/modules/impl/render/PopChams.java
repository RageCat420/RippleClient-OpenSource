package dev.ripple.mod.modules.impl.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ripple.asm.accessors.IEntity;
import dev.ripple.asm.accessors.ILivingEntity;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import dev.ripple.mod.modules.settings.impl.EnumSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.TotemEvent;
import dev.ripple.api.utils.math.MathUtil;
import dev.ripple.mod.modules.Module;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PopChams extends Module {
    public static PopChams INSTANCE;
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Simple));
    private final BooleanSetting secondLayer = add(new BooleanSetting("SecondLayer", false));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(0, 150, 255)));
    private final SliderSetting ySpeed = add(new SliderSetting("YSpeed", 0, -10, 10, 0.01));
    private final SliderSetting aSpeed = add(new SliderSetting("AlphaSpeed", 8, 0, 30));
    private final SliderSetting rotSpeed = add(new SliderSetting("RotationSpeed", 0f, 0f, 6, 0.01));
    private final CopyOnWriteArrayList<Person> popList = new CopyOnWriteArrayList<>();

    public PopChams() {
        super("PopChams", Category.Render);
        setChinese("爆图腾特效");
        INSTANCE = this;
    }

    private enum Mode {
        Simple, Textured
    }

    @Override
    public void onUpdate() {
        popList.forEach(person -> person.update(popList));
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        if (mode.is(Mode.Simple)) RenderSystem.defaultBlendFunc();
        else RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        popList.forEach(person -> renderEntity(stack, person.player, person.modelPlayer, person.getTexture(), person.getAlpha()));
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @EventHandler
    private void onTotemPop(TotemEvent e) {
        if (e.getPlayer().equals(mc.player) || mc.world == null) return;

        PlayerEntity entity = new PlayerEntity(mc.world, BlockPos.ORIGIN, e.getPlayer().bodyYaw, new GameProfile(e.getPlayer().getUuid(), e.getPlayer().getName().getString())) {
            @Override public boolean isSpectator() {
                return false;
            }
            @Override public boolean isCreative() {
                return false;
            }
        };

        entity.copyPositionAndRotation(e.getPlayer());
        entity.bodyYaw = e.getPlayer().bodyYaw;
        entity.headYaw = e.getPlayer().headYaw;
        entity.handSwingProgress = e.getPlayer().handSwingProgress;
        //entity.handSwingTicks = e.getPlayer().handSwingTicks;
        entity.limbAnimator.setSpeed(e.getPlayer().limbAnimator.getSpeed());
        entity.limbAnimator.pos = e.getPlayer().limbAnimator.getPos();
        entity.setPose(e.getPlayer().getPose());
        entity.setSneaking(e.getPlayer().isSneaking());
        entity.leaningPitch = e.getPlayer().getLeaningPitch(mc.getRenderTickCounter().getTickDelta(true));
        entity.setFlag(7, e.getPlayer().getFlag(7));
        entity.fallFlyingTicks = e.getPlayer().getFallFlyingTicks();
        //不确定⬇
        entity.setLivingFlag(4, e.getPlayer().isUsingRiptide());
        entity.setVelocity(e.getPlayer().getVelocity());
        entity.touchingWater = e.getPlayer().isTouchingWater();
        entity.vehicle = e.getPlayer().getVehicle();
        Byte playerModel = e.getPlayer().getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
        entity.getDataTracker().set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);

        entity.capeX = e.getPlayer().capeX;
        entity.capeY = e.getPlayer().capeY;
        entity.capeZ = e.getPlayer().capeZ;

        popList.add(new Person(entity, ((AbstractClientPlayerEntity) e.getPlayer()).getSkinTextures().texture()));
    }

    private void renderEntity(MatrixStack matrices, LivingEntity entity, PlayerEntityModel<PlayerEntity> modelBase, Identifier texture, int alpha) {
        modelBase.leftPants.visible = secondLayer.getValue();
        modelBase.rightPants.visible = secondLayer.getValue();
        modelBase.leftSleeve.visible = secondLayer.getValue();
        modelBase.rightSleeve.visible = secondLayer.getValue();
        modelBase.jacket.visible = secondLayer.getValue();
        modelBase.hat.visible = secondLayer.getValue();

        modelBase.sneaking = entity.isSneaking();

        double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        ((IEntity) entity).setPos(entity.getPos().add(0, (double) ySpeed.getValueFloat() / 50, 0));

        matrices.push();
        matrices.translate((float) x, (float) y, (float) z);

        float yRotYaw = ((alpha / 255f) * 360f * rotSpeed.getValueFloat());
        yRotYaw = yRotYaw == 0 ? 0 : (float) interpolate(yRotYaw, yRotYaw - (((aSpeed.getValueInt() / 255f) * 360f * rotSpeed.getValueFloat())), mc.getRenderTickCounter().getTickDelta(true));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180 - entity.bodyYaw + yRotYaw)));

        modelBase.handSwingProgress = entity.getHandSwingProgress(1);

        float j = ((ILivingEntity) entity).getLeaningPitch();
        if (entity.isFallFlying()) {

            float k = entity.getPitch();
            float l;
            float m;

            l = (float) entity.getFallFlyingTicks() + entity.bodyYaw + entity.getYaw();
            m = MathHelper.clamp(l * l / 100.0F, 0.0F, 1.0F);
            if (!entity.isUsingRiptide()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * (-90.0F - k)));
            }

            Vec3d vec3d = entity.getRotationVec(1);
            Vec3d vec3d2 = entity.getVelocity();
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double n = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double o = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Math.signum(o) * Math.acos(n))));
            }
        } else if (j > 0.0F) {
            float k = entity.getPitch();
            float l = entity.isTouchingWater() ? -90.0F - k : -90.0F;
            float m = MathHelper.lerp(j, 0.0F, l);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            if (entity.isInSwimmingPose()) {
                matrices.translate(0.0F, -1.0F, 0.3F);
            }
        }

        prepareScale(matrices);

        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getRenderTickCounter().getTickDelta(true));

        float limbSpeed = Math.min(entity.limbAnimator.getSpeed(), 1f);
        //float limbSpeed = 0;
        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), limbSpeed, entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());

        //modelBase.handSwingProgress = entity.getHandSwingProgress(mc.getRenderTickCounter().getTickDelta(true));

        modelBase.riding = entity.hasVehicle();

        BufferBuilder buffer;
        if (mode.is(Mode.Textured)) {
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        RenderSystem.setShaderColor((float) color.getValue().getRed() / 255, (float) color.getValue().getGreen() / 255, (float) color.getValue().getBlue() / 255, alpha / 255f);

        modelBase.render(matrices, buffer, 10, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrices.pop();
    }

    private static void prepareScale(MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    private class Person {
        private final PlayerEntity player;
        private final PlayerEntityModel<PlayerEntity> modelPlayer;
        private Identifier texture;
        private int alpha;

        public Person(PlayerEntity player, Identifier texture) {
            this.player = player;
            modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
            modelPlayer.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));
            alpha = color.getValue().getAlpha();
            this.texture = texture;
        }

        public void update(CopyOnWriteArrayList<Person> arrayList) {
            //player.setPos(player.getX(), player.getY() + (double) ySpeed.getValueInt() / 50, player.getZ());
            if (alpha <= 0) {
                arrayList.remove(this);
                player.kill();
                player.remove(Entity.RemovalReason.KILLED);
                player.onRemoved();
                return;
            }
            alpha -= aSpeed.getValueInt();
        }

        public int getAlpha() {
            return (int) MathUtil.clamp(alpha, 0, 255);
        }

        public Identifier getTexture() {
            return texture;
        }
    }
}