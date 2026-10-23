package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.cheecky.module.Modules;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * X-Ray: Smart Culling überspringt Chunk-Abschnitte hinter "massivem" Stein. Ohne Abschalten
 * fehlen Erze in Abschnitten, die man normalerweise nicht sehen könnte. Diese Datei gilt für 26.2.
 */
@Mixin(Camera.class)
public class CameraMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/client/renderer/state/level/CameraRenderState;F)V",
		at = @At("RETURN"))
	private void cheecky$disableSmartCull(CameraRenderState cameraState, float partialTicks, CallbackInfo ci) {
		if (Modules.XRAY.isEnabled())
			cameraState.smartCull = false;
	}
}
