package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cheecky.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** NoSlow für Blöcke: Seelensand, Honig (Geschwindigkeitsfaktor) und Spinnweben, Beerenbüsche (Festhängen). */
@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "getBlockSpeedFactor()F", at = @At("RETURN"), cancellable = true)
	private void cheecky$noBlockSlowdown(CallbackInfoReturnable<Float> cir) {
		if (isLocalPlayerWithNoSlow() && cir.getReturnValueF() < 1)
			cir.setReturnValue(1F);
	}

	@Inject(method = "makeStuckInBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V",
		at = @At("HEAD"), cancellable = true)
	private void cheecky$noStuckInBlock(BlockState state, Vec3 speedMultiplier, CallbackInfo ci) {
		if (isLocalPlayerWithNoSlow())
			ci.cancel();
	}

	private boolean isLocalPlayerWithNoSlow() {
		return (Object) this == Minecraft.getInstance().player && Modules.NO_SLOW.isEnabled();
	}
}
