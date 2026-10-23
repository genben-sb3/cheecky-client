package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.cheecky.module.Modules;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

/** Freecam: die Maus dreht die Kamera, nicht den Körper. */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@WrapOperation(method = "turnPlayer(D)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void cheecky$turnCamera(LocalPlayer player, double deltaYaw, double deltaPitch, Operation<Void> original) {
		if (Modules.FREECAM.isActive())
			Modules.FREECAM.turn(deltaYaw, deltaPitch);
		else
			original.call(player, deltaYaw, deltaPitch);
	}
}
