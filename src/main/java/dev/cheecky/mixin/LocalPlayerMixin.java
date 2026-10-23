package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.cheecky.module.Modules;
import net.minecraft.client.player.LocalPlayer;

/** NoSlow: kein Abbremsen und weiter Sprinten beim Benutzen von Items (Essen, Bogen, Schild). */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@WrapOperation(method = "aiStep()V", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z", ordinal = 0))
	private boolean cheecky$aiStepItemSlowdown(LocalPlayer player, Operation<Boolean> original) {
		return !Modules.NO_SLOW.isEnabled() && original.call(player);
	}

	@WrapOperation(method = "modifyInput(Lnet/minecraft/world/phys/Vec2;)Lnet/minecraft/world/phys/Vec2;", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0))
	private boolean cheecky$inputItemSlowdown(LocalPlayer player, Operation<Boolean> original) {
		return !Modules.NO_SLOW.isEnabled() && original.call(player);
	}

	@WrapOperation(method = "canStartSprinting()Z", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z", ordinal = 0))
	private boolean cheecky$sprintItemSlowdown(LocalPlayer player, Operation<Boolean> original) {
		return !Modules.NO_SLOW.isEnabled() && original.call(player);
	}
}
