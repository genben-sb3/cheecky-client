package dev.cheecky.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.cheecky.module.Modules;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * X-Ray für Indigo, den Renderer der Fabric API. Mit Fabric API installiert läuft fast alles hier
 * durch statt durch ModelBlockRenderer.
 */
@Mixin(targets = "net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl", remap = false)
public class AltModelBlockRendererImplMixin {
	@Shadow
	private BlockState blockState;

	@Inject(method = "shouldCullFace", at = @At("HEAD"), cancellable = true, require = 0)
	private void cheecky$shouldCullFace(Direction face, CallbackInfoReturnable<Boolean> cir) {
		Boolean draw = Modules.XRAY.shouldDrawFace(blockState);
		if (draw != null)
			cir.setReturnValue(!draw);
	}
}
