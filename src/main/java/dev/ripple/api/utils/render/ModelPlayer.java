package dev.ripple.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ripple.api.utils.math.MathUtil;
import dev.ripple.asm.accessors.ILivingEntity;
import dev.ripple.mod.modules.impl.render.PopChams;
import dev.ripple.mod.modules.settings.impl.ColorSetting;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;

import static dev.ripple.api.utils.Wrapper.mc;
import static dev.ripple.mod.modules.impl.render.PopChams.interpolate;

public class ModelPlayer extends PlayerEntityModel<PlayerEntity> {
    public final PlayerEntity player;
    private static final Vector4f pos1 = new Vector4f();
    private static final Vector4f pos2 = new Vector4f();
    private static final Vector4f pos3 = new Vector4f();
    private static final Vector4f pos4 = new Vector4f();

    public ModelPlayer(PlayerEntity player) {
        super(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
        this.player = player;
        leftPants.visible = false;
        rightPants.visible = false;
        leftSleeve.visible = false;
        rightSleeve.visible = false;
        jacket.visible = false;
        hat.visible = false;
        getHead().scale(new Vector3f(-0.05f, -0.05f, -0.05f));
        sneaking = player.isInSneakingPose();
    }

    public void render(MatrixStack matrices, ColorSetting fill, ColorSetting line, float r, int a) {
        render(matrices, fill, line, 1, 0, 1, 0, false, false, r, a);
    }

    public void render(MatrixStack matrices, ColorSetting fill, ColorSetting line, double alpha, double yOffset, double scale, double yaw, boolean noLimb, boolean forceSneaking, float rotSpeed, int aSpeed) {
        if (forceSneaking) {
            sneaking = true;
        }
        double x = player.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = player.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY() + yOffset;
        double z = player.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();

        matrices.push();
        matrices.translate((float) x, (float) y, (float) z);

        //yaw = ((alpha / 255f) * 360f * rotSpeed);
        //yaw = yaw == 0 ? 0 : (float) interpolate(yaw, yaw - (((aSpeed / 255f) * 360f * rotSpeed)), mc.getRenderTickCounter().getTickDelta(true));


        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180 - player.bodyYaw + (float) yaw)));

        handSwingProgress = player.getHandSwingProgress(1);
        float j = ((ILivingEntity) player).getLeaningPitch();
        if (player.isFallFlying()) {

            float k = player.getPitch();
            float l;
            float m;

            l = (float) player.getFallFlyingTicks() + player.bodyYaw + (float) yaw;
            m = MathHelper.clamp(l * l / 100.0F, 0.0F, 1.0F);
            if (!player.isUsingRiptide()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * (-90.0F - k)));
            }

            Vec3d vec3d = player.getRotationVec(1);
            Vec3d vec3d2 = player.getVelocity();
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double n = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double o = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Math.signum(o) * Math.acos(n))));
            }
        } else if (j > 0.0F) {
            float k = player.getPitch();
            float l = player.isTouchingWater() ? -90.0F - k : -90.0F;
            float m = MathHelper.lerp(j, 0.0F, l);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            if (player.isInSwimmingPose()) {
                matrices.translate(0.0F, -1.0F, 0.3F);
            }
        }

        //matrices.scale(-1.0F, -1.0F, 1.0F);
        //matrices.translate(0.0F, -1.401F, 0.0F);
        //matrices.scale((float) scale * .93f, (float) scale * .93f, (float) scale * .93f);
        //PopChams.prepareScale(matrices);

        animateModel(player, noLimb ? 0 : player.limbAnimator.getPos(), noLimb ? 0 : player.limbAnimator.getSpeed(), mc.getRenderTickCounter().getTickDelta(true));
        setAngles(player, noLimb ? 0 : player.limbAnimator.getPos(), noLimb ? 0 : player.limbAnimator.getSpeed(), player.age, player.headYaw - player.bodyYaw, player.getPitch());

        riding = player.hasVehicle();
        //RenderSystem.enableBlend();
        //RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        this.getHeadParts().forEach((modelPart) -> render(matrices, modelPart, fill, line, alpha, false));
        this.getBodyParts().forEach((modelPart) -> render(matrices, modelPart, fill, line, alpha, false));

        matrices.pop();

        //RenderSystem.disableBlend();
        //RenderSystem.enableDepthTest();
    }

    public static void render(MatrixStack matrices, ModelPart part, ColorSetting fill, ColorSetting line, double alpha, boolean texture) {
        if (!part.visible || (part.cuboids.isEmpty() && part.children.isEmpty())) return;
        matrices.push();
        part.rotate(matrices);
        for (ModelPart.Cuboid cuboid : part.cuboids) render(matrices, cuboid, fill, line, alpha, texture);
        for (ModelPart child : part.children.values()) render(matrices, child, fill, line, alpha, texture);
        matrices.pop();
    }

    public static void render(MatrixStack matrices, ModelPart.Cuboid cuboid, ColorSetting fill, ColorSetting line, double alpha, boolean texture) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        for (ModelPart.Quad quad : cuboid.sides) {
            pos1.set(quad.vertices[0].pos.x / 16, quad.vertices[0].pos.y / 16, quad.vertices[0].pos.z / 16, 1);
            pos1.mul(matrix);

            pos2.set(quad.vertices[1].pos.x / 16, quad.vertices[1].pos.y / 16, quad.vertices[1].pos.z / 16, 1);
            pos2.mul(matrix);

            pos3.set(quad.vertices[2].pos.x / 16, quad.vertices[2].pos.y / 16, quad.vertices[2].pos.z / 16, 1);
            pos3.mul(matrix);

            pos4.set(quad.vertices[3].pos.x / 16, quad.vertices[3].pos.y / 16, quad.vertices[3].pos.z / 16, 1);
            pos4.mul(matrix);
            if (fill.booleanValue) {
                Color color = fill.getValue();
                float a = (float) (color.getAlpha() / 255f * alpha);
                float r = color.getRed() / 255f;
                float g = color.getGreen() / 255f;
                float b = color.getBlue() / 255f;
                BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, texture ? VertexFormats.POSITION_TEXTURE_COLOR : VertexFormats.POSITION_COLOR);
                buffer.vertex(pos1.x, pos1.y, pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, b, a);
                buffer.vertex(pos2.x, pos2.y, pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, b, a);

                buffer.vertex(pos2.x, pos2.y, pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, b, a);
                buffer.vertex(pos3.x, pos3.y, pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, b, a);

                buffer.vertex(pos3.x, pos3.y, pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, b, a);
                buffer.vertex(pos4.x, pos4.y, pos4.z).texture(quad.vertices[3].u, quad.vertices[3].v).color(r, g, b, a);

                buffer.vertex(pos1.x, pos1.y, pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, b, a);
                buffer.vertex(pos1.x, pos1.y, pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, b, a);

                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
            if (line.booleanValue) {
                Color color = line.getValue();
                float a = (float) (color.getAlpha() / 255f * alpha);
                float r = color.getRed() / 255f;
                float g = color.getGreen() / 255f;
                float b = color.getBlue() / 255f;
                BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, texture ? VertexFormats.POSITION_TEXTURE_COLOR : VertexFormats.POSITION_COLOR);

                buffer.vertex(pos1.x, pos1.y, pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, b, a);
                buffer.vertex(pos2.x, pos2.y, pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, b, a);

                buffer.vertex(pos2.x, pos2.y, pos2.z).texture(quad.vertices[1].u, quad.vertices[1].v).color(r, g, b, a);
                buffer.vertex(pos3.x, pos3.y, pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, b, a);

                buffer.vertex(pos3.x, pos3.y, pos3.z).texture(quad.vertices[2].u, quad.vertices[2].v).color(r, g, b, a);
                buffer.vertex(pos4.x, pos4.y, pos4.z).texture(quad.vertices[3].u, quad.vertices[3].v).color(r, g, b, a);

                buffer.vertex(pos4.x, pos4.y, pos4.z).texture(quad.vertices[3].u, quad.vertices[3].v).color(r, g, b, a);
                buffer.vertex(pos1.x, pos1.y, pos1.z).texture(quad.vertices[0].u, quad.vertices[0].v).color(r, g, b, a);

                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
        }
    }
}