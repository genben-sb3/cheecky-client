package dev.cheecky.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cheecky.module.Modules;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** X-Ray für den Vanilla-Blockrenderer (läuft, wenn Indigo nicht zuständig ist). */
@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
	/** Versteckte Blöcke gar nicht erst zusammenbauen. */
	@Inject(method = {
		"tesselateFlat(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLjava/util/List;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
		"tesselateAmbientOcclusion(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLjava/util/List;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"},
		at = @At("HEAD"), cancellable = true)
	private void cheecky$hideBlock(BlockQuadOutput output, float x, float y, float z, List<BlockStateModelPart> parts,
		BlockAndTintGetter level, BlockState state, BlockPos pos, CallbackInfo ci) {
		if (Boolean.FALSE.equals(Modules.XRAY.shouldDrawFace(state)))
			ci.cancel();
	}

	/** Sichtbare Blöcke (Erze) mit allen Seiten zeichnen, auch wenn Stein daneben liegt. */
	@Inject(method = "shouldRenderFace(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
		at = @At("HEAD"), cancellable = true)
	private void cheecky$shouldRenderFace(BlockAndTintGetter level, BlockState state, Direction direction, BlockPos neighborPos,
		CallbackInfoReturnable<Boolean> cir) {
		Boolean draw = Modules.XRAY.shouldDrawFace(state);
		if (draw != null)
			cir.setReturnValue(draw);
	}
}
