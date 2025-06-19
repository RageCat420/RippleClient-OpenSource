package dev.ripple.asm.mixins;

import dev.ripple.Ripple;
import dev.ripple.core.impl.ShaderManager;
import dev.ripple.mod.modules.impl.player.Freecam;
import dev.ripple.mod.modules.impl.render.Chams;
import dev.ripple.mod.modules.impl.render.Shader;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.ripple.api.utils.Wrapper.mc;
import static org.lwjgl.opengl.GL11C.*;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
	@Unique
	boolean renderingChams = false;
	@Unique
	boolean renderingEntity = false;

	@Inject(method = "renderEntity", at = @At("HEAD"))
	private void injectChamsForEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		if (Chams.INSTANCE.isOn() && Chams.INSTANCE.throughWall.getValue()) {
			if (Chams.INSTANCE.chams(entity)) {
				if (renderingEntity) {
					mc.getBufferBuilders().getEntityVertexConsumers().draw();
					renderingEntity = false;
				}
				glEnable(GL_POLYGON_OFFSET_FILL);
				glPolygonOffset(1f, -1000000F);
				renderingChams = true;
			} else {
				renderingEntity = true;
			}
		}
	}

	@Inject(method = "renderEntity", at = @At("RETURN"))
	private void injectChamsForEntityPost(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		if (Chams.INSTANCE.isOn() && Chams.INSTANCE.throughWall.getValue()) {
			if (renderingChams) {
				mc.getBufferBuilders().getEntityVertexConsumers().draw();
				glPolygonOffset(1f, 1000000F);
				glDisable(GL_POLYGON_OFFSET_FILL);
				renderingChams = false;
			}
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
	void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
		ShaderManager.Shader shaders = Shader.INSTANCE.mode.getValue();
		if (Shader.INSTANCE.isOn() && mc.world != null) {
			Ripple.SHADER.setupShader(shaders, Ripple.SHADER.getShaderOutline(shaders));
		} else {
			instance.render(tickDelta);
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
	private boolean renderSetupTerrainModifyArg(boolean spectator) {
		return Freecam.INSTANCE.isOn() || spectator;
	}
}
