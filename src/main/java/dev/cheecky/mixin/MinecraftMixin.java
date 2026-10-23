package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cheecky.module.Modules;
import net.minecraft.client.Minecraft;

/** Freecam: kein Schlagen, Abbauen oder Benutzen, sonst trifft man etwas neben dem Körper. */
@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "startAttack()Z", at = @At("HEAD"), cancellable = true)
	private void cheecky$noAttack(CallbackInfoReturnable<Boolean> cir) {
		if (Modules.FREECAM.isActive())
			cir.setReturnValue(false);
	}

	@Inject(method = "continueAttack(Z)V", at = @At("HEAD"), cancellable = true)
	private void cheecky$noBreak(boolean leftClick, CallbackInfo ci) {
		if (Modules.FREECAM.isActive())
			ci.cancel();
	}

	@Inject(method = "startUseItem()V", at = @At("HEAD"), cancellable = true)
	private void cheecky$noUse(CallbackInfo ci) {
		if (Modules.FREECAM.isActive())
			ci.cancel();
	}
}
