package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import dev.cheecky.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	/** AutoTool: vor dem Abbauen das beste Werkzeug wählen. */
	@Inject(method = {"startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
		"continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"}, at = @At("HEAD"))
	private void cheecky$beforeDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null)
			Modules.AUTO_TOOL.onDestroyBlock(mc.level.getBlockState(pos));
	}

	/**
	 * FastBreak: Abbau-Fortschritt pro Tick vervielfachen. Nur in continueDestroyBlock, denn in
	 * startDestroyBlock steckt der Aufruf in einem Lambda.
	 */
	@ModifyExpressionValue(method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	private float cheecky$modifyDestroyProgress(float progress) {
		return Modules.FAST_BREAK.modifyProgress(progress);
	}
}
