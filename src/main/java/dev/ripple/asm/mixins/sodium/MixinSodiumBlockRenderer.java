package dev.ripple.asm.mixins.sodium;

import dev.ripple.mod.modules.impl.render.XRay;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class)
public class MixinSodiumBlockRenderer {
    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/model/color/ColorProviderRegistry;getColorProvider(Lnet/minecraft/block/Block;)Lnet/caffeinemc/mods/sodium/client/model/color/ColorProvider;", shift = At.Shift.AFTER), cancellable = true)
    private void onRenderModel(BakedModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
        if (XRay.INSTANCE.isOn()) {
            if (!XRay.INSTANCE.isCheckableOre(state.getBlock())) {
                ci.cancel();
            }
        }
    }
}