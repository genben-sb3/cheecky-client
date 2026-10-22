package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cheecky.module.Modules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** X-Ray: keine Schattierung, damit Erze im Dunkeln gut sichtbar sind. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {
	@Inject(method = "getShadeBrightness(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F",
		at = @At("RETURN"), cancellable = true)
	private void cheecky$fullShade(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (Modules.XRAY.isEnabled())
			cir.setReturnValue(1F);
	}
}
