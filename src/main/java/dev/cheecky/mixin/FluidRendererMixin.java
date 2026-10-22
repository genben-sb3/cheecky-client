package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.cheecky.module.Modules;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** X-Ray für Wasser und Lava. */
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	private static final String TESSELATE = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V";

	@Inject(method = TESSELATE, at = @At("HEAD"), cancellable = true)
	private void cheecky$hideFluid(BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState,
		FluidState fluidState, CallbackInfo ci) {
		if (Boolean.FALSE.equals(Modules.XRAY.shouldDrawFace(fluidState.createLegacyBlock())))
			ci.cancel();
	}

	/** Sichtbare Flüssigkeiten nicht von (unsichtbaren) Nachbarblöcken verdecken lassen. */
	@WrapOperation(method = TESSELATE, at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/block/FluidRenderer;isFaceOccludedByNeighbor(Lnet/minecraft/core/Direction;FLnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean cheecky$isFaceOccluded(Direction direction, float height, BlockState neighbor, Operation<Boolean> original) {
		if (Modules.XRAY.isEnabled())
			return false;
		return original.call(direction, height, neighbor);
	}
}
